package vn.lapro;
import javax.swing.SwingUtilities;
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JarCloner frame = new JarCloner();
            frame.setVisible(true);
        });
    }
}