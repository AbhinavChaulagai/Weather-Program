package com.company;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherApp extends JFrame {
    private JTextField cityField;
    private JTextField tempField;
    private JLabel conditionLabel;

    public WeatherApp() {
        super("Weather App");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new FlowLayout());

        add(new JLabel("City:"));
        cityField = new JTextField(15);
        add(cityField);

        JButton fetchBtn = new JButton("Fetch");
        add(fetchBtn);

        add(new JLabel("Temp:"));
        tempField = new JTextField(15);
        tempField.setEditable(false);
        add(tempField);

        add(new JLabel("Condition:"));
        conditionLabel = new JLabel();
        add(conditionLabel);

        fetchBtn.addActionListener(e -> fetchWeather(cityField.getText().trim()));

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void fetchWeather(String city) {
        if (city.isEmpty()) return;

        new Thread(() -> {
            try {
                String apiKey = "aff8d712f5876f77a8f28740454f53a5";
                String urlStr = "https://api.openweathermap.org/data/2.5/weather?q="
                        + city + "&appid=" + apiKey;
                HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
                conn.setRequestMethod("GET");

                int responseCode = conn.getResponseCode();
                InputStream stream = (responseCode == 200)
                        ? conn.getInputStream()
                        : conn.getErrorStream();

                BufferedReader in = new BufferedReader(new InputStreamReader(stream));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) sb.append(line);
                in.close();

                String json = sb.toString();

                if (responseCode == 200) {
                    String condition = parse(json, "\"main\":\"", "\""); // first occurrence: weather[0].main
                    String tempStr = parse(json, "\"temp\":", ",");
                    double kelvin = Double.parseDouble(tempStr);
                    double celsius = kelvin - 273.15;

                    SwingUtilities.invokeLater(() -> {
                        tempField.setText(String.format("%.2f °C", celsius));
                        conditionLabel.setText(condition);
                    });
                } else {
                    SwingUtilities.invokeLater(() -> {
                        tempField.setText("City not found");
                        conditionLabel.setText("Error");
                        System.err.println("API error: " + json);
                    });
                }

                conn.disconnect();
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    tempField.setText("Network error");
                    conditionLabel.setText("Failed");
                });
                ex.printStackTrace();
            }
        }).start();
    }

    private String parse(String src, String start, String end) {
        int s = src.indexOf(start);
        if (s == -1) return "N/A";
        s += start.length();
        int e = src.indexOf(end, s);
        if (e == -1) return "N/A";
        return src.substring(s, e);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(WeatherApp::new);
    }
}
