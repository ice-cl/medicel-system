package com.medical.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medical.system.config.DeepSeekProperties;
import com.medical.system.entity.HealthData;
import com.medical.system.entity.Patient;
import com.medical.system.service.AiHealthAnalysisService;
import com.medical.system.service.HealthDataService;
import com.medical.system.service.PatientService;
import com.medical.system.vo.AiHealthAnalysisVO;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AiHealthAnalysisServiceImpl implements AiHealthAnalysisService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final Map<String, String> HEALTH_TYPE_LABELS = Map.of(
            "BLOOD_PRESSURE_HIGH", "收缩压",
            "BLOOD_PRESSURE_LOW", "舒张压",
            "BLOOD_SUGAR", "血糖",
            "WEIGHT", "体重",
            "HEART_RATE", "心率"
    );

    private final PatientService patientService;
    private final HealthDataService healthDataService;
    private final DeepSeekProperties deepSeekProperties;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    public AiHealthAnalysisServiceImpl(PatientService patientService,
                                       HealthDataService healthDataService,
                                       DeepSeekProperties deepSeekProperties,
                                       ObjectMapper objectMapper,
                                       RestTemplateBuilder restTemplateBuilder) {
        this.patientService = patientService;
        this.healthDataService = healthDataService;
        this.deepSeekProperties = deepSeekProperties;
        this.objectMapper = objectMapper;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout((int) Duration.ofSeconds(10).toMillis());
        requestFactory.setReadTimeout((int) Duration.ofSeconds(60).toMillis());
        this.restTemplate = restTemplateBuilder
                .requestFactory(() -> requestFactory)
                .build();
    }

    @Override
    public AiHealthAnalysisVO analyzeCurrentPatient(Long userId) {
        Patient patient = patientService.getOne(new LambdaQueryWrapper<Patient>()
                .eq(Patient::getUserId, userId)
                .eq(Patient::getDeleted, 0)
                .last("limit 1"));
        if (patient == null) {
            throw new RuntimeException("未找到当前患者信息");
        }
        return analyzePatient(patient, "PATIENT");
    }

    @Override
    public AiHealthAnalysisVO analyzePatientForClinician(Long patientId) {
        Patient patient = patientService.getOne(new LambdaQueryWrapper<Patient>()
                .eq(Patient::getId, patientId)
                .eq(Patient::getDeleted, 0)
                .last("limit 1"));
        if (patient == null) {
            throw new RuntimeException("患者不存在");
        }
        return analyzePatient(patient, "CLINICIAN");
    }

    private AiHealthAnalysisVO analyzePatient(Patient patient, String audience) {
        List<HealthData> records = healthDataService.list(new LambdaQueryWrapper<HealthData>()
                .eq(HealthData::getPatientId, patient.getId())
                .eq(HealthData::getDeleted, 0)
                .orderByDesc(HealthData::getRecordTime)
                .last("limit 30"));

        AiHealthAnalysisVO result = new AiHealthAnalysisVO();
        result.setPatientId(patient.getId());
        result.setPatientName(patient.getName());
        result.setAudience(audience);
        result.setRecordCount(records.size());
        result.setLatestRecordTime(records.isEmpty() ? null : formatDateTime(records.get(0).getRecordTime()));
        result.setDisclaimer("AI 分析仅基于当前录入的指标做初步判断，不能替代医生面诊、检查和正式诊断。");

        if (records.isEmpty()) {
            result.setAnalysis(buildNoDataAnalysis(patient, audience));
            return result;
        }

        String analysis = requestDeepSeekAnalysis(patient, records, audience);
        result.setAnalysis(analysis);
        return result;
    }

    private String requestDeepSeekAnalysis(Patient patient, List<HealthData> records, String audience) {
        if (!StringUtils.hasText(deepSeekProperties.getApiKey())) {
            throw new RuntimeException("未配置 DeepSeek API Key，请先设置环境变量 DEEPSEEK_API_KEY");
        }

        String systemPrompt = buildSystemPrompt(audience);
        String userPrompt = buildUserPrompt(patient, records, audience);

        try {
            JsonNode requestBody = objectMapper.createObjectNode()
                    .put("model", deepSeekProperties.getModel())
                    .put("temperature", deepSeekProperties.getTemperature())
                    .put("max_tokens", deepSeekProperties.getMaxTokens())
                    .set("messages", objectMapper.createArrayNode()
                            .add(objectMapper.createObjectNode()
                                    .put("role", "system")
                                    .put("content", systemPrompt))
                            .add(objectMapper.createObjectNode()
                                    .put("role", "user")
                                    .put("content", userPrompt)));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(deepSeekProperties.getApiKey());

            ResponseEntity<String> response = restTemplate.postForEntity(
                    normalizeBaseUrl(deepSeekProperties.getBaseUrl()) + "/chat/completions",
                    new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers),
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException(extractErrorMessage(response.getBody(), response.getStatusCode().value()));
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            String content = root.path("choices").path(0).path("message").path("content").asText();
            if (!StringUtils.hasText(content)) {
                throw new RuntimeException("DeepSeek 未返回有效分析结果");
            }
            return content.trim();
        } catch (IOException e) {
            throw new RuntimeException("解析 DeepSeek 响应失败", e);
        } catch (RestClientException e) {
            throw new RuntimeException("调用 DeepSeek 接口失败：" + e.getMessage(), e);
        }
    }

    private String buildSystemPrompt(String audience) {
        if ("CLINICIAN".equals(audience)) {
            return """
                    你是一名谨慎的 AI 临床辅助分析助手。请仅根据提供的数据做初步判断，不要虚构，不要下确定性诊断。
                    请用中文输出，控制在 220 字以内，并严格按以下小节组织：
                    1. 总体判断
                    2. 关键指标与趋势
                    3. 临床关注点
                    4. 建议干预与复查方向
                    输出要求：
                    - 每节 1 句，尽量短
                    - 明确指出数据不足
                    - 结尾补一句“以上仅为 AI 辅助分析，不能替代临床诊断。”
                    """;
        }

        return """
                你是一名谨慎的 AI 健康助手。请仅根据提供的数据做初步分析，不要虚构，不要替代医生确诊。
                请用中文输出，控制在 180 字以内，并严格按以下小节组织：
                1. 当前身体指标概览
                2. 需要注意的地方
                3. 日常建议
                4. 什么时候建议及时就医
                输出要求：
                - 每节 1 句，语言通俗
                - 明确指出数据不足
                - 结尾补一句“以上仅供健康参考，不能替代医生面诊。”
                """;
    }

    private String buildUserPrompt(Patient patient, List<HealthData> records, String audience) {
        StringBuilder builder = new StringBuilder();
        builder.append("分析视角：")
                .append("CLINICIAN".equals(audience) ? "医生/临床辅助" : "患者健康解读")
                .append('\n');
        builder.append("患者基本资料：\n")
                .append("- 姓名：").append(defaultValue(patient.getName())).append('\n')
                .append("- 性别：").append(resolveGender(patient.getGender())).append('\n')
                .append("- 年龄：").append(resolveAge(patient.getBirthDate())).append('\n')
                .append("- 过敏史：").append(defaultValue(patient.getAllergyHistory())).append('\n');
        builder.append('\n');
        builder.append("最近健康指标摘要：\n");
        builder.append(buildMetricsSummary(records));
        builder.append('\n');
        builder.append("只基于这些摘要输出简洁结论。");
        return builder.toString();
    }

    private String buildMetricsSummary(List<HealthData> records) {
        Map<String, List<HealthData>> grouped = records.stream()
                .sorted(Comparator.comparing(HealthData::getRecordTime))
                .collect(Collectors.groupingBy(HealthData::getType, LinkedHashMap::new, Collectors.toList()));

        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, List<HealthData>> entry : grouped.entrySet()) {
            List<HealthData> typeRecords = entry.getValue();
            HealthData latest = typeRecords.get(typeRecords.size() - 1);
            builder.append("- ")
                    .append(resolveHealthTypeLabel(entry.getKey()))
                    .append("：")
                    .append(formatDecimal(latest.getValue()))
                    .append(latest.getUnit() == null ? "" : " " + latest.getUnit())
                    .append("，时间 ").append(formatDateTime(latest.getRecordTime()))
                    .append("，样本 ").append(typeRecords.size());

            if (typeRecords.size() > 1) {
                builder.append("，均值 ").append(calculateAverage(typeRecords))
                        .append("，趋势 ").append(resolveTrend(typeRecords));
            } else {
                builder.append("，趋势 样本不足");
            }
            builder.append('\n');
        }
        return builder.toString();
    }

    private String buildNoDataAnalysis(Patient patient, String audience) {
        String patientName = defaultValue(patient.getName());
        if ("CLINICIAN".equals(audience)) {
            return """
                    1. 总体判断
                    当前没有可用于分析的健康指标记录，暂时无法形成可靠的趋势判断。

                    2. 关键指标与趋势
                    目前缺少血压、血糖、体重、心率等连续数据，无法评估波动情况。

                    3. 临床关注点
                    建议先补齐近期基础生命体征和关键慢病相关指标，再进行 AI 辅助分析。

                    4. 建议干预与复查方向
                    可优先录入血压、心率、体重，以及带有采样条件说明的血糖数据。

                    5. 与患者沟通建议
                    可提醒患者先连续记录一段时间的指标，再做趋势判断会更有参考价值。

                    以上仅为 AI 辅助分析，不能替代临床诊断。
                    """;
        }

        return "1. 当前身体指标概览\n目前还没有检测到 " + patientName + " 的健康指标记录，暂时无法判断身体状态。\n\n"
                + "2. 需要注意的地方\n没有血压、血糖、体重、心率等基础数据时，AI 无法给出有依据的趋势分析。\n\n"
                + "3. 日常建议\n建议先录入最近一段时间的血压、血糖、体重或心率数据，再重新进行分析。\n\n"
                + "4. 什么时候建议及时就医\n如果近期已经出现头晕、胸闷、心慌、明显乏力等不适，不要等待数据录入，应及时线下就医。\n\n"
                + "以上仅供健康参考，不能替代医生面诊。";
    }

    private String extractErrorMessage(String responseBody, int statusCode) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String message = root.path("error").path("message").asText();
            if (StringUtils.hasText(message)) {
                return "DeepSeek 调用失败(" + statusCode + ")：" + message;
            }
        } catch (Exception ignored) {
            // Ignore and use fallback message below.
        }
        return "DeepSeek 调用失败，HTTP 状态码：" + statusCode;
    }

    private String normalizeBaseUrl(String baseUrl) {
        String normalized = StringUtils.hasText(baseUrl) ? baseUrl.trim() : "https://api.deepseek.com";
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private String resolveGender(Integer gender) {
        if (gender == null) {
            return "未提供";
        }
        if (gender == 1) {
            return "男";
        }
        if (gender == 2) {
            return "女";
        }
        return "未提供";
    }

    private String resolveAge(LocalDate birthDate) {
        if (birthDate == null) {
            return "未提供";
        }
        return Period.between(birthDate, LocalDate.now()).getYears() + "岁";
    }

    private String resolveHealthTypeLabel(String type) {
        return HEALTH_TYPE_LABELS.getOrDefault(type, type);
    }

    private String resolveTrend(List<HealthData> records) {
        if (records.size() < 2) {
            return "样本不足";
        }

        List<BigDecimal> values = records.stream()
                .map(HealthData::getValue)
                .collect(Collectors.toCollection(ArrayList::new));
        BigDecimal first = values.get(0);
        BigDecimal last = values.get(values.size() - 1);
        BigDecimal diff = last.subtract(first).abs();

        if (diff.compareTo(BigDecimal.ONE) <= 0) {
            return "基本平稳";
        }
        return last.compareTo(first) > 0 ? "整体上升" : "整体下降";
    }

    private String calculateAverage(List<HealthData> records) {
        BigDecimal total = records.stream()
                .map(HealthData::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal average = total.divide(BigDecimal.valueOf(records.size()), 2, RoundingMode.HALF_UP);
        return formatDecimal(average);
    }

    private String formatDecimal(BigDecimal value) {
        return value == null ? "--" : value.stripTrailingZeros().toPlainString();
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "--" : dateTime.format(DATE_TIME_FORMATTER);
    }

    private String defaultValue(String value) {
        return StringUtils.hasText(value) ? value : "未提供";
    }
}
