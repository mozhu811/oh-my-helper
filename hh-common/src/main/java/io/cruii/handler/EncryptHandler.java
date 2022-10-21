package io.cruii.handler;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class EncryptHandler implements TypeHandler<String> {

    private static final String SECRET_KEY;

    static {
        try {
            InputStream stream = ResourceUtil.getStream("config.properties");
            Properties properties = new Properties();
            properties.load(stream);
            SECRET_KEY = properties.getProperty("secret.key");
        } catch (IOException e) {
            throw new RuntimeException("获取加密秘钥失败", e);
        }
    }

    @Override
    public void setParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
        if (parameter != null) {
            AES aes = SecureUtil.aes(SECRET_KEY.getBytes());
            ps.setString(i, aes.encryptHex(parameter));
        } else {
            ps.setNull(i, Types.VARCHAR);
        }
    }

    @Override
    public String getResult(ResultSet rs, String columnName) throws SQLException {
        String result = rs.getString(columnName);
        if (result == null) {
            return null;
        }
        AES aes = SecureUtil.aes(SECRET_KEY.getBytes());
        return aes.decryptStr(result);
    }

    @Override
    public String getResult(ResultSet rs, int columnIndex) throws SQLException {
        String result = rs.getString(columnIndex);
        if (result == null) {
            return null;
        }

        AES aes = SecureUtil.aes(SECRET_KEY.getBytes());
        return aes.decryptStr(result);

    }

    @Override
    public String getResult(CallableStatement cs, int columnIndex) throws SQLException {
        String result = cs.getString(columnIndex);
        if (result == null) {
            return null;
        }

        AES aes = SecureUtil.aes(SECRET_KEY.getBytes());
        return aes.decryptStr(result);
    }
}
