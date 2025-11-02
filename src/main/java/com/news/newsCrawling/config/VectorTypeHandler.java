package com.news.newsCrawling.config;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.*;
import java.util.Arrays;

public class VectorTypeHandler extends BaseTypeHandler<float[]> {


    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, float[] parameter, JdbcType jdbcType) throws SQLException {
        // float[]를 PostgreSQL의 vector 형식으로 변환
        String vectorString = Arrays.toString(parameter); // 대괄호 유지
        ps.setObject(i, vectorString, Types.OTHER);
    }

    @Override
    public float[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parseVector(rs.getString(columnName));
    }

    @Override
    public float[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parseVector(rs.getString(columnIndex));
    }

    @Override
    public float[] getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parseVector(cs.getString(columnIndex));
    }

    private float[] parseVector(String vectorString) {
        if (vectorString == null || vectorString.isEmpty()) {
            return null;
        }
        // PostgreSQL vector 형식이 올바른지 확인
        vectorString = vectorString.trim();
        if (!vectorString.startsWith("[") || !vectorString.endsWith("]")) {
            throw new IllegalArgumentException("Invalid vector format: " + vectorString);
        }
        try {
            // 대괄호 제거 후 파싱
            String[] values = vectorString.substring(1, vectorString.length() - 1).split(",");
            float[] vector = new float[values.length];
            for (int i = 0; i < values.length; i++) {
                vector[i] = Float.parseFloat(values[i].trim());
            }
            return vector;
        } catch (NumberFormatException e) {
            throw new RuntimeException("Failed to parse vector: " + vectorString, e);
        }
    }
}