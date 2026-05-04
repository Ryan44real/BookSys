package com.tem.booksys.service;

public interface AiService {

    /** AI 智能荐书 */
    String recommend(Integer userId, String query, String model);

    /** AI 监控统计 */
    Object getAiStats();
}
