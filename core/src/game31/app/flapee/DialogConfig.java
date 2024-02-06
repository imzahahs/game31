package game31.app.flapee;

public class DialogConfig {

    public String text;

    public String leftStarText;
    public String rightStarText;

    public String positiveButtonText;
    public String negativeButtonText;

    public Runnable positiveButtonAction;
    public Runnable negativeButtonAction;

    public DialogConfig(String text, String leftStarText, String rightStarText, String positiveButtonText, String negativeButtonText) {
        this.text = text;
        this.leftStarText = leftStarText;
        this.rightStarText = rightStarText;
        this.positiveButtonText = positiveButtonText;
        this.negativeButtonText = negativeButtonText;
    }
}
