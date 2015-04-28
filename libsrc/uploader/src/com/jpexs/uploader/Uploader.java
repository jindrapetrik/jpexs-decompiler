package com.jpexs.uploader;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.UIManager;

/**
 *
 * @author JPEXS
 */
public class Uploader {

    private static class MultipartUtility {

        private final String boundary;
        private static final String LINE_FEED = "\r\n";
        private HttpURLConnection httpConn;
        private String charset;
        private OutputStream outputStream;
        private PrintWriter writer;

        /**
         * This constructor initializes a new HTTP POST request with content
         * type is set to multipart/form-data
         *
         * @param requestURL
         * @param charset
         * @throws IOException
         */
        public MultipartUtility(String requestURL, String charset)
                throws IOException {
            this.charset = charset;

            // creates a unique boundary based on time stamp
            boundary = Long.toHexString(System.currentTimeMillis());

            URL url = new URL(requestURL);
            httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setUseCaches(false);
            httpConn.setDoOutput(true); // indicates POST method
            httpConn.setDoInput(true);
            httpConn.setRequestProperty("User-Agent", "JPEXS Uploader");
            httpConn.setRequestProperty("Content-Type",
                    "multipart/form-data; boundary=" + boundary);
            outputStream = httpConn.getOutputStream();
            writer = new PrintWriter(new OutputStreamWriter(outputStream, charset),
                    true);
        }

        /**
         * Adds a form field to the request
         *
         * @param name field name
         * @param value field value
         */
        public void addFormField(String name, String value) {
            writer.append("--" + boundary).append(LINE_FEED);
            writer.append("Content-Disposition: form-data; name=\"" + name + "\"")
                    .append(LINE_FEED);
            writer.append("Content-Type: text/plain; charset=" + charset).append(
                    LINE_FEED);
            writer.append(LINE_FEED);
            writer.append(value).append(LINE_FEED);
            writer.flush();
        }

        /**
         * Adds a upload file section to the request
         *
         * @param fieldName name attribute in <input type="file" name="..." />
         * @param uploadFile a File to be uploaded
         * @throws IOException
         */
        public void addFilePart(String fieldName, File uploadFile)
                throws IOException {
            String fileName = uploadFile.getName();
            writer.append("--" + boundary).append(LINE_FEED);
            writer.append(
                    "Content-Disposition: form-data; name=\"" + fieldName
                    + "\"; filename=\"" + fileName + "\"")
                    .append(LINE_FEED);
            writer.append(
                    "Content-Type: "
                    + URLConnection.guessContentTypeFromName(fileName))
                    .append(LINE_FEED);
            writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
            writer.append(LINE_FEED);
            writer.flush();

            FileInputStream inputStream = new FileInputStream(uploadFile);
            byte[] buffer = new byte[4096];
            int bytesRead = -1;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
            inputStream.close();

            writer.append(LINE_FEED);
            writer.flush();
        }

        /**
         * Completes the request and receives response from the server.
         *
         * @return a list of Strings as response in case the server returned
         * status OK, otherwise an exception is thrown.
         * @throws IOException
         */
        public boolean finish(List<String> response) throws IOException {
            response.clear();
            //writer.append(LINE_FEED).flush();
            writer.append("--" + boundary + "--").append(LINE_FEED);
            writer.close();

            // checks server's status code first
            int status = httpConn.getResponseCode();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    httpConn.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                response.add(line);
            }
            reader.close();
            httpConn.disconnect();

            return status == HttpURLConnection.HTTP_OK;
        }
    }

    private static List<String> types = new ArrayList<>();
    private static List<String> names = new ArrayList<>();
    private static List<String> values = new ArrayList<>();
    private static List<String> labels = new ArrayList<>();

    public static void main(String[] args) {

        if (args.length < 1) {
            System.err.println("1");
            System.exit(1);
        }

        String charset = "UTF-8";
        String requestURL = args[0];

        for (int i = 1; i < args.length; i++) {
            if (args[i].equals("-field")) {
                types.add("field");
                names.add(args[i + 1]);
                values.add(args[i + 2]);
                labels.add("");
                i += 2;
            }
            if (args[i].equals("-emptyfield")) {
                types.add("field");
                names.add(args[i + 1]);
                values.add("");
                labels.add("");
                i += 2;
            }
            if (args[i].equals("-textarea")) {
                types.add("textarea");
                names.add(args[i + 1]);
                labels.add(args[i + 2]);
                values.add("");
                i += 2;
            }
            if (args[i].equals("-file")) {
                types.add("file");
                names.add(args[i + 1]);
                values.add(args[i + 2]);
                labels.add("");
                i += 2;
            }
        }

        List<JTextArea> texts = new ArrayList<>();
        List<JLabel> textLabels = new ArrayList<>();
        List<Integer> textIndices = new ArrayList<>();

        for (int i = 0; i < types.size(); i++) {
            if (types.get(i).equals("textarea")) {
                JTextArea t = new JTextArea();
                t.setPreferredSize(new Dimension(400, 100));
                texts.add(t);
                textLabels.add(new JLabel(labels.get(i)));
                textIndices.add(i);
            }
        }
        if (!texts.isEmpty()) {
            JPanel pan = new JPanel();
            pan.setLayout(new BoxLayout(pan, BoxLayout.Y_AXIS));
            for (int i = 0; i < texts.size(); i++) {
                textLabels.get(i).setAlignmentX(0f);
                pan.add(textLabels.get(i));
                texts.get(i).setAlignmentX(0f);
                pan.add(texts.get(i));
            }

            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {

            }

            if (JOptionPane.showConfirmDialog(null, pan, "Enter values", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
                System.exit(1);
            }
            for (int i = 0; i < texts.size(); i++) {
                int index = textIndices.get(i);
                types.set(index, "field");
                values.set(index, texts.get(i).getText());
            }
        }

        try {
            MultipartUtility multipart = new MultipartUtility(requestURL, charset);
            for (int i = 0; i < types.size(); i++) {
                if (types.get(i).equals("field")) {
                    multipart.addFormField(names.get(i), values.get(i));
                }
                if (types.get(i).equals("file")) {
                    multipart.addFilePart(names.get(i), new File(values.get(i)));
                }
            }

            List<String> response = new ArrayList<>();
            boolean ok = multipart.finish(response);

            for (String line : response) {
                if (ok) {
                    System.out.println(line);
                } else {
                    System.err.println(line);
                }
            }
            System.exit(ok ? 0 : 1);
        } catch (IOException ex) {
            System.exit(1);
        }
    }
}
