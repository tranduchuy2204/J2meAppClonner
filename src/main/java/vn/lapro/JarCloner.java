package vn.lapro;


import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import java.io.InputStream;
import java.nio.file.*;
import java.text.MessageFormat;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public class JarCloner extends JFrame {
    private JTextField inputJarPathField;
    private JTextField outputFolderField;
    private JTextField numberOfCopiesField;
    private JTextArea logArea;

    public JarCloner() {
        setTitle(AppPreferences.getAppName() + " Version: " + AppPreferences.CURRENT_VERSION);
        setSize(700, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initComponents();
        addComponents();
    }

    private void initComponents() {
        inputJarPathField = new JTextField();
        outputFolderField = new JTextField();
        numberOfCopiesField = new JTextField();
        numberOfCopiesField.setText("2");
        logArea = new JTextArea();
        logArea.setEditable(false);
        inputJarPathField.setText(AppPreferences.getInputJarPath());
        outputFolderField.setText(AppPreferences.getOutputFolder());
    }

    private void addComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.2;
        gbc.anchor = GridBagConstraints.WEST;
        add(new JLabel("Input JAR Path:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.8;
        inputJarPathField.setPreferredSize(new Dimension(10, 30));
        add(inputJarPathField, gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        JButton chooseJarButton = new JButton("Choose File");
        chooseJarButton.addActionListener(e -> chooseJarFile());
        chooseJarButton.setPreferredSize(new Dimension(120, 30));
        add(chooseJarButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.2;
        add(new JLabel("Output Folder:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.8;
        outputFolderField.setPreferredSize(new Dimension(10, 30));
        add(outputFolderField, gbc);

        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        JButton chooseFolderButton = new JButton("Choose Folder");
        chooseFolderButton.addActionListener(e -> chooseOutputFolder());
        chooseFolderButton.setPreferredSize(new Dimension(120, 30));
        add(chooseFolderButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0.2;
        add(new JLabel("Number of Copies:"), gbc);

        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        JButton cloneButton = new JButton("Start");
        cloneButton.addActionListener(e -> {
            try {
                logArea.setText("");
                cloneAndModifyJars();
            } catch (IOException ex) {
                log("Lỗi: " + ex.getMessage());
                throw new RuntimeException(ex);
            }
        });
        cloneButton.setPreferredSize(new Dimension(120, 30));
        add(cloneButton, gbc);


        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0.8;
        numberOfCopiesField.setPreferredSize(new Dimension(10, 30));
        add(numberOfCopiesField, gbc);

        // Log Area
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        add(new JScrollPane(logArea), gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0;
        gbc.weighty = 0;
        add(new JLabel(MessageFormat.format("© {0} Powered By {1}", "" + AppPreferences.CURRENT_YEAR, AppPreferences.getAuthor())), gbc);
    }

    private void chooseJarFile() {
        JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        fileChooser.setDialogTitle("Choose JAR File");
        fileChooser.setFileFilter(new FileNameExtensionFilter("JAR Files", "jar"));
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            inputJarPathField.setText(selectedFile.getAbsolutePath());
            AppPreferences.setInputJarPath(selectedFile.getAbsolutePath());
        }
    }

    private void chooseOutputFolder() {
        JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        fileChooser.setDialogTitle("Choose Output Folder");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFolder = fileChooser.getSelectedFile();
            outputFolderField.setText(selectedFolder.getAbsolutePath());
            AppPreferences.setOutputFolder(selectedFolder.getAbsolutePath());
        }
    }

    private void cloneAndModifyJars() throws IOException {
        String inputJarPath = inputJarPathField.getText();
        String outputFolder = outputFolderField.getText();
        int numberOfCopies = Integer.parseInt(numberOfCopiesField.getText());

        File sourceJar = new File(inputJarPath);
        File destinationFolder = new File(outputFolder);

        if (destinationFolder.exists()) {
            FileUtils.cleanDirectory(destinationFolder);
        } else {
            destinationFolder.mkdirs();
        }

        String sourceJarName = sourceJar.getName();
        new Thread(() -> {
            for (int i = 1; i <= numberOfCopies; i++) {
                try {
                    String destinationJarName = sourceJarName.replace(".jar", "_copy" + i + ".jar");
                    File destinationJar = new File(destinationFolder, destinationJarName);
                    FileUtils.copyFile(sourceJar, destinationJar);
                    modifyManifest(destinationJar.toPath(), i);
                    log("Đã thêm và sửa đổi " + destinationJar.getPath());
                } catch (IOException ignored) {
                }
            }
            log("Đã nhân bản " + numberOfCopies + "  file JAR thành công.");
        }).start();
    }

    private void modifyManifest(Path filePath, final int index) throws IOException {
        if (filePath.toString().endsWith(".jar")) {
            Path tempJar = Paths.get(filePath.toString().replace(".jar", "_temp.jar"));

            try (JarOutputStream jarOutputStream = new JarOutputStream(Files.newOutputStream(tempJar));
                 FileSystem fileSystem = FileSystems.newFileSystem(filePath, null)) {
                Path manifestPath = fileSystem.getPath("META-INF", "MANIFEST.MF");
                Manifest sourceManifest = new Manifest(Files.newInputStream(manifestPath));
                JarEntry manifestEntry = new JarEntry("META-INF/MANIFEST.MF");
                Manifest manifest = (Manifest) sourceManifest.clone();
                // Sửa đổi Manifest
                String oldMidletName = manifest.getMainAttributes().getValue("MIDlet-Name");
                String oldMidlet1 = manifest.getMainAttributes().getValue("MIDlet-1");
                manifest.getMainAttributes().putValue("MIDlet-Name", oldMidletName + "_" + index);
                String[] items = oldMidlet1.split(",");
                items[0] += "_" + index;
                manifest.getMainAttributes()
                        .putValue("MIDlet-1", String.join(",", items));

                jarOutputStream.putNextEntry(manifestEntry);
                manifest.write(jarOutputStream);
                jarOutputStream.closeEntry();

                try (InputStream is = Files.newInputStream(filePath);
                     JarInputStream jarInputStream = new JarInputStream(is)) {

                    JarEntry entry;
                    while ((entry = jarInputStream.getNextJarEntry()) != null) {
                        if (!"META-INF/MANIFEST.MF".equals(entry.getName())) {
                            jarOutputStream.putNextEntry(new JarEntry(entry.getName()));
                            byte[] buffer = new byte[4096];
                            int bytesRead;
                            while ((bytesRead = jarInputStream.read(buffer)) != -1) {
                                jarOutputStream.write(buffer, 0, bytesRead);
                            }
                            jarOutputStream.closeEntry();
                        }
                    }
                }
            }
            Files.delete(filePath);
            Files.move(tempJar, filePath);
        }
    }

    private void log(String message) {
        logArea.append(message + "\n");
    }
}
