import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Stack;

public class Calculator extends JFrame {
    JButton digits[] = {
            new JButton(" 0 "), new JButton(" 1 "), new JButton(" 2 "),
            new JButton(" 3 "), new JButton(" 4 "), new JButton(" 5 "),
            new JButton(" 6 "), new JButton(" 7 "), new JButton(" 8 "),
            new JButton(" 9 ")
    };

    JButton operators[] = {
            new JButton(" + "), new JButton(" - "), new JButton(" * "),
            new JButton(" / "), new JButton(" = "), new JButton(" C "),
            new JButton(" ( "), new JButton(" ) ")  // Adăugăm butoanele pentru paranteze
    };

    JTextArea area = new JTextArea(3, 5);
    String expression = "";

    public static void main(String[] args) {
        Calculator calculator = new Calculator();
        calculator.setSize(300, 400);
        calculator.setTitle(" Java-Calc, PP Lab1 ");
        calculator.setResizable(false);
        calculator.setVisible(true);
        calculator.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public Calculator() {
        add(new JScrollPane(area), BorderLayout.NORTH);
        JPanel buttonpanel = new JPanel();
        buttonpanel.setLayout(new FlowLayout());

        // Adăugăm butoanele de numere
        for (int i = 0; i < 10; i++) buttonpanel.add(digits[i]);

        // Adăugăm butoanele de operatori și paranteze
        for (int i = 0; i < 8; i++) buttonpanel.add(operators[i]);

        add(buttonpanel, BorderLayout.CENTER);
        area.setForeground(Color.BLACK);
        area.setBackground(Color.WHITE);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setEditable(false);

        // Evenimente pentru butoanele de numere
        for (int i = 0; i < 10; i++) {
            final int finalI = i;
            digits[i].addActionListener(e -> {
                expression += Integer.toString(finalI);
                area.setText(expression);
            });
        }

        // Evenimente pentru butoanele de operatori și paranteze
        for (int i = 0; i < 8; i++) {
            final int finalI = i;
            operators[i].addActionListener(e -> {
                if (finalI == 5) {  // Butonul C - Clear
                    expression = "";
                    area.setText(expression);
                } else if (finalI == 4) {  // Butonul =
                    try {
                        double result = evaluateExpression(expression);
                        area.setText(String.valueOf(result));
                    } catch (Exception ex) {
                        area.setText("Error");
                    }
                } else if (finalI == 6) {  // Butonul ( - Adăugăm paranteza stângă
                    expression += "(";
                    area.setText(expression);
                } else if (finalI == 7) {  
                    expression += ")";
                    area.setText(expression);
                } else {  // Operatori
                    expression += " " + operators[finalI].getText().trim() + " ";
                    area.setText(expression);
                }
            });
        }
    }

    // Evaluarea expresiei matematice utilizând arbori
    public double evaluateExpression(String expr) throws Exception {
        // Parsăm și construim arborele de expresie
        ExpressionTree tree = buildExpressionTree(expr);
        return evaluateTree(tree);
    }

    // Construirea arborelui de expresie
    public ExpressionTree buildExpressionTree(String expr) throws Exception {
        Stack<ExpressionTree> operandStack = new Stack<>();
        Stack<Character> operatorStack = new Stack<>();
        StringBuilder currentNumber = new StringBuilder();

        for (int i = 0; i < expr.length(); i++) {
            char c = expr.charAt(i);

            if (Character.isDigit(c)) {
                currentNumber.append(c);  // Construim numărul
            } else if (c == ' ') {
                continue;
            } else if (c == '(') {
                operatorStack.push(c);  // Începem un nou subarbore
            } else if (c == ')') {
                // Calculăm până la '('
                while (operatorStack.peek() != '(') {
                    processOperator(operatorStack, operandStack);
                }
                operatorStack.pop();  // Eliminăm '('
            } else if (isOperator(c)) {
                // Procesăm operatorii
                while (!operatorStack.isEmpty() && precedence(c) <= precedence(operatorStack.peek())) {
                    processOperator(operatorStack, operandStack);
                }
                operatorStack.push(c);
            }

            // Când întâlnim un număr complet
            if (currentNumber.length() > 0 && (i == expr.length() - 1 || !Character.isDigit(expr.charAt(i + 1)))) {
                operandStack.push(new ExpressionTree(Double.parseDouble(currentNumber.toString())));
                currentNumber = new StringBuilder();
            }
        }

        // Procesăm restul operatorilor
        while (!operatorStack.isEmpty()) {
            processOperator(operatorStack, operandStack);
        }

        return operandStack.pop();
    }

    // Evaluarea arborelui de expresie
    public double evaluateTree(ExpressionTree tree) {
        if (tree.isLeaf()) {
            return tree.getValue();
        }

        double leftValue = evaluateTree(tree.getLeft());
        double rightValue = evaluateTree(tree.getRight());

        switch (tree.getOperator()) {
            case '+':
                return leftValue + rightValue;
            case '-':
                return leftValue - rightValue;
            case '*':
                return leftValue * rightValue;
            case '/':
                return leftValue / rightValue;
            default:
                throw new UnsupportedOperationException("Operator necunoscut");
        }
    }

    // Procesarea unui operator
    private void processOperator(Stack<Character> operatorStack, Stack<ExpressionTree> operandStack) {
        char operator = operatorStack.pop();
        ExpressionTree right = operandStack.pop();
        ExpressionTree left = operandStack.pop();
        operandStack.push(new ExpressionTree(operator, left, right));
    }

    // Verificăm dacă un caracter este operator
    private boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    // Determinăm precedența operatorilor
    private int precedence(char operator) {
        switch (operator) {
            case '+':
            case '-':
                return 1;
            case '*':
            case '/':
                return 2;
            default:
                return -1;
        }
    }

    // Arborele de expresie
    class ExpressionTree {
        private double value;
        private char operator;
        private ExpressionTree left, right;

        // Constructor pentru noduri cu număr (frunze)
        public ExpressionTree(double value) {
            this.value = value;
        }

        // Constructor pentru noduri cu operator (interior)
        public ExpressionTree(char operator, ExpressionTree left, ExpressionTree right) {
            this.operator = operator;
            this.left = left;
            this.right = right;
        }

        // Obține valoarea unui nod (dacă este frunză)
        public double getValue() {
            return value;
        }

        // Obține operatorul unui nod (dacă este operator)
        public char getOperator() {
            return operator;
        }

        // Obține subarborele stâng
        public ExpressionTree getLeft() {
            return left;
        }

        
        public ExpressionTree getRight() {
            return right;
        }

        // Verifică dacă nodul este o frunză (nu are copii)
        public boolean isLeaf() {
            return left == null && right == null;
        }
    }
}
