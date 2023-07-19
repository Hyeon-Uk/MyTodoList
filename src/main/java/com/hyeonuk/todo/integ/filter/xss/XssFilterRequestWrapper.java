package com.hyeonuk.todo.integ.filter.xss;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.*;
import java.net.URLDecoder;
import java.util.Map;

public class XssFilterRequestWrapper extends HttpServletRequestWrapper {
    private byte[] raw;

    public String xssFilter(String strData) {
        try {
            if (strData != null) {
                strData = URLDecoder.decode(strData.replaceAll("\\<", "&lt;").replaceAll("\\>", "&gt;").replaceAll("\\(", "&#40;").replaceAll("\\)", "&#41;"), "UTF-8");
            }
            return strData;
        } catch (UnsupportedEncodingException e) {//지원하지않는 인코딩타입. 지원하지 않는 타입이라면 null을 반환하도록
            return null;
        }
    }

    public XssFilterRequestWrapper(HttpServletRequest request) {
        super(request);

        try {
            if ("post".equalsIgnoreCase(request.getMethod())
                    && "application/json".equalsIgnoreCase(request.getContentType())
                    || "multipart/form-data".equalsIgnoreCase(request.getContentType())) {

                InputStream is = request.getInputStream();

                this.raw = xssFilter(new String(is.readAllBytes())).getBytes();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (this.raw == null) {
            return super.getInputStream();
        }
        final ByteArrayInputStream bais = new ByteArrayInputStream(this.raw);

        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener listener) {

            }

            @Override
            public int read() throws IOException {
                return bais.read();
            }
        };
    }

    @Override
    public String getQueryString() {
        return xssFilter(super.getQueryString());
    }

    @Override
    public String getParameter(String name) {
        return xssFilter(super.getParameter(name));
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> params = super.getParameterMap();
        if (params != null) {
            params.forEach((key, value) -> {
                for (int i = 0; i < value.length; i++) {
                    value[i] = xssFilter(value[i]);
                }
            });
        }
        return params;
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] params = super.getParameterValues(name);
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                params[i] = xssFilter(params[i]);
            }
        }
        return params;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(this.getInputStream(), "UTF_8"));
    }
}
