package Veritas;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;
import javax.swing.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

/**
 *
 * @author Tobias Oskarsson och Adnan Dervisevic
 */
public class Calculator extends JPanel implements ActionListener, KeyEventDispatcher
{
    /**
     * En enumeration som innehåller de flesta tecken på miniräknaren.
     * @author Tobias Oskarsson och Adnan Dervisevic
     */
    private enum Symbols
    {
        Digit,
        Add,
        Sub,
        Multiply,
        Divide,
        OneDividedByX,
        SquareRoot,
        Percentage,
    }

    /**
     * KeyEventDispatacher som hjälper till att kolla tangentnertryckningar.
     */
    private KeyEventDispatcher myKeyEventDispatcher = new DefaultFocusManager();

    /**
     * En vector som håller reda på alla ICalcEventListeners.
     */
    private Vector listeners = new Vector();

    /**
     * En vector som håller reda på alla ICalcConversionEventListeners.
     */
    private Vector conversionListeners = new Vector();

    /**
     * En double som håller reda på talet.
     */
    private double calculation;

    /**
     * En stringBuffer som håller koll på talet man skriver in.
     */
    private StringBuffer botValue;

    /**
     * En boolean som är true om nästa siffra är början på ett nytt tal.
     */
    private boolean newNumber;

    /**
     * Är denna true betyder det att vi har börjat vår uträkning.
     */
    private boolean initializedCalculations;

    /**
     * Är denna true så har vi använt ett kommatecken.
     */
    private boolean comma;

    /**
     * Är freeze satt till true ska man inte kunna göra något förutom att trycka clear och clearEntry.
     */
    private boolean freeze;

    /**
     * Ska tangentbordet användas?
     */
    private boolean keyboard;

    /**
     * Senaste knappen.
     */
    private Symbols latestButton;

    /**
     * Senaste operatorn.
     */
    private Symbols latestOperator;

    /**
     * En double som håller reda på talet man har i minnet.
     */
    private double memory;

    /**
     * En hash tabell som använder en sträng som nyckel och en Double som value.
     * Används sedan för att lätt kunna convertera mellan olika valutor.
     */
    private Hashtable<String, Double> currency = new Hashtable<String, Double>();

    /**
     * En getter för resultatet.
     * @return Returnerar det uträknade resultatet.
     */
    public double getResults()
    {
        return this.calculation;
    }

    /**
     * Aktiverar eller deaktiverar tangentbordet.
     * @param enabled Om true så aktiveras tangentbordet aktiverat.
     */
    public void setKeyboard(boolean enabled)
    {
        this.keyboard = enabled;
    }

    /**
     * En getter för tangentbordet.
     * @return Returnerar true om tangentbordet är aktiverat, annars false.
     */
    public boolean isKeyboard()
    {
        return this.keyboard;
    }

    /**
     * Metod för att lägga till CalcEventListeners.
     * @param listener Den ICalcEventListener som ska läggas till.
     */
    public synchronized void addCalcEventListener(ICalcEventListener listener)
    {
       this.listeners.add(listener);
    }

    /**
     * Metod för att ta bort CalcEventListeners.
     * @param listener Den ICalcEventListener som ska tas bort.
     */
    public synchronized void removeCalcEventListener(ICalcEventListener listener)
    {
        this.listeners.remove(listener);
    }

    /**
     * Metod för att lägga till CalcConversionEventListeners.
     * @param listener Den ICalcConversionEventListener som ska läggas till.
     */
    public synchronized void addCalcConversionEventListener(ICalcConversionEventListener listener)
    {
       this.conversionListeners.add(listener);
    }

    /**
     * Metod för att ta bort CalcConversionEventListeners.
     * @param listener Den ICalcConversionEventListener som ska tas bort.
     */
    public synchronized void removeCalcConversionEventListener(ICalcConversionEventListener listener)
    {
        this.conversionListeners.remove(listener);
    }

    /**
     * Metod för att köra igång alla ICalcEventListeners calculationDone metoder.
     */
    private void fireCalcEvent()
    {
        // Kopierar alla lyssnare.
        Vector listeners;
        synchronized (this)
        {
            listeners = (Vector)this.listeners.clone();
        }

        // Loopar igenom alla lyssnare och kör metoden.
        for (int i = 0; i < listeners.size(); i++)
        {
            ICalcEventListener listener = (ICalcEventListener)listeners.elementAt(i);
            listener.calculationDone(this.calculation);
        }
    }

    /**
     * Metod för att köra igång alla ICalcEventListeners calculationDone metoder.
     */
    private void fireConversionEvent(double currency)
    {
        // Kopierar alla lyssnare.
        Vector listeners;
        synchronized (this)
        {
            listeners = (Vector)this.conversionListeners.clone();
        }

        // Loopar igenom alla lyssnare och kör metoden.
        for (int i = 0; i < listeners.size(); i++)
        {
            ICalcConversionEventListener listener = (ICalcConversionEventListener)listeners.elementAt(i);
            listener.conversionDone(currency);
        }
    }

    /**
     * Metod som körs varje gång ett KeyEvent körs.
     * @param e KeyEventet som körs.
     * @return Returnerar true om eventen blev behandlat, annars false.
     */
    @Override
    public boolean dispatchKeyEvent(KeyEvent e)
    {
        // Kolla om keyEventet är KEY_PRESSED.
        if (e.getID() == KeyEvent.KEY_PRESSED)
        {
            // Om keyboard är true
            if (keyboard)
            {
                // Kolla vilken knapp som trycktes ner.
                switch (e.getKeyCode())
                {
                    // If the user press ENTER
                    case 10:
                        this.equalBtnActionPerformed(null);
                        return true;

                    case 96:
                    case 48:
                        this.zeroBtnActionPerformed(null);
                        return true;

                    case 97:
                    case 49:
                        this.AddNumber("1");
                        return true;

                    case 98:
                    case 50:
                        this.AddNumber("2");
                        return true;

                    case 99:
                    case 51:
                        this.AddNumber("3");
                        return true;

                    case 100:
                    case 52:
                        this.AddNumber("4");
                        return true;

                    case 101:
                    case 53:
                        this.AddNumber("5");
                        return true;

                    case 102:
                    case 54:
                        this.AddNumber("6");
                        return true;

                    case 103:
                    case 55:
                        this.AddNumber("7");
                        return true;

                    case 104:
                    case 56:
                        this.AddNumber("8");
                        return true;

                    case 105:
                    case 57:
                        this.AddNumber("9");
                        return true;

                    case 107:
                    case 521:
                        this.addBtnActionPerformed(null);
                        return true;

                    case 109:
                        this.subBtnActionPerformed(null);
                        return true;

                    case 106:
                        this.multiplyBtnActionPerformed(null);
                        return true;

                    case 111:
                        this.divideBtnActionPerformed(null);
                        return true;

                    case 110:
                        this.commaBtnActionPerformed(null);
                        return true;

                    case 8:
                        this.backBtnActionPerformed(null);
                        return true;

                    case 127:
                        this.clearEntryBtnActionPerformed(null);
                        return true;

                        // Vi har inte behandlat nertryckningen.
                    default:
                        return false;
                }
            }
        }

        // Vi har inte behandlat nertryckningen.
        return false;
    }

    /**
     * En metod som körs varje gång en action utförs.
     * @param e ActionEventet som utfördes.
     */
    @Override
    public void actionPerformed(ActionEvent e)
    {
        // Om freeze är true ska inget hända.
        if (freeze)
            return;

        JButton button = (JButton)e.getSource();

        // Om knappen var någon siffra mellan 1-9 lägg till den.
        if (button == this.oneBtn || button == this.twoBtn || button == this.threeBtn ||
                button == this.fourBtn || button == this.fiveBtn || button == this.sixBtn ||
                button == this.sevenBtn || button == this.eightBtn || button == this.nineBtn)
        {
            this.AddNumber(button.getText());
        }
    }

    /**
     * Metod som sätter value.
     * Metoden kollar så att det är en double man skrivit in och
     * @param number Numret som man ska sätta botValue till.
     */
    public void SetValue(String number)
    {
        String str = "\\d+\\.\\d+|\\d+";

        // Pattern och Matcher för att jämföra mot regex stringen.
        Pattern pattern;
        Matcher matcher;

        // Kompilerar pattern och matchar nummret.
        pattern = Pattern.compile(str);
        matcher = pattern.matcher(number);

        // Om nummbret är en double eller int så kör den vidare.
        if (matcher.matches()) {
            this.botValue.delete(0, this.botValue.length());
            this.botValue.append(number);
            this.botValueTxt.setText(this.botValue.toString());
            this.newNumber = true;

            if (this.botValue.indexOf(".") > -1)
                this.comma = true;

            this.latestButton = Symbols.Digit;
        }
    }

    /**
     * Metod som converterar värdet till den valda valutan.
     */
    public void Convert()
    {
        // Funktion som konverterar.
        this.convertBtnActionPerformed(null);
    }

    /**
     * En metod för att lägga till ett nummer.
     * @param number Nummret man vill lägga till.
     */
    private void AddNumber(String number)
    {
        // Om det är ett nytt nummer så tar vi bort allt i botValue.
        if (this.newNumber)
        {
            this.botValue.delete(0, this.botValue.length()); // Ta bort allt i botValue.
            this.botValue.append("0.0"); // Lägg till 0.0 i botValue.
            this.newNumber = false; // nästa siffra ska inte börja ett nytt tal.
        }

        if (!this.comma) // Om vi inte har skrivit ett kommatecken.
        {
            if (this.botValue.indexOf(".") > -1) // Om kommatecknet finns ändå
            {
                // Om siffran före . tecknet är en nolla så tar vi bort allt före . tecknet
                if (this.botValue.substring(0, this.botValue.indexOf(".")).equalsIgnoreCase("0"))
                    this.botValue.delete(0, this.botValue.indexOf(".")); // Så tar vi bort allt i botValue som är framför . tecknet.

                this.botValue.insert(this.botValue.indexOf("."), number); // Lägg till talet före . tecknet
            }
            else // Om kommatecknet inte finns så läggs siffran på i slutet.
                this.botValue.append(number); // // Lägg till talet i slutet av siffran.
        }
        else
        {
            // Om vi skrivit ett kommatecken.
            // Då tar vi bort allt efter kommatecknet.
            if (this.botValue.substring(this.botValue.indexOf(".")).equalsIgnoreCase("0"))
                this.botValue.delete(this.botValue.indexOf(".") + 1, this.botValue.length()); // Ta bort allt efter kommatecknet.

            this.botValue.append(number); // // Lägg till talet i slutet av siffran.
        }

        this.latestButton = Symbols.Digit; // Ändrar senaste knappen til digit.
        this.botValueTxt.setText(this.botValue.toString()); // Vi sätter botValueTxt till botValue.
    }

    /**
     * Funktion för att addera, används för att inte behöva skriva sama kod flera gånger.
     */
    private void Add()
    {
        // Räkna
        this.calculation += Double.parseDouble(this.botValue.toString());
        // Ta bort allt i botValue.
        this.botValue.delete(0, this.botValue.length());
        // Lägg till svaret i botValue.
        this.botValue.append(this.calculation);
        // Uppdatera botValueTxt.
        this.botValueTxt.setText(this.botValue.toString());
    }

    /**
     * Funktion för att subtrahera, används för att inte behöva skriva sama kod flera gånger.
     */
    private void Sub()
    {
        // Räkna
        this.calculation -= Double.parseDouble(this.botValue.toString());
        // Ta bort allt i botValue.
        this.botValue.delete(0, this.botValue.length());
        // Lägg till svaret i botValue.
        this.botValue.append(this.calculation);
        // Uppdatera botValueTxt.
        this.botValueTxt.setText(this.botValue.toString());
    }

    /**
     * Funktion för att multiplicera, används för att inte behöva skriva sama kod flera gånger.
     */
    private void Multiply()
    {
        // Räkna
        this.calculation *= Double.parseDouble(this.botValue.toString());
        // Ta bort allt i botValue.
        this.botValue.delete(0, this.botValue.length());
        // Lägg till svaret i botValue.
        this.botValue.append(this.calculation);
        // Uppdatera botValueTxt.
        this.botValueTxt.setText(this.botValue.toString());
    }

    /**
     * Funktion för att dividera, används för att inte behöva skriva sama kod flera gånger.
     */
    private void Divide()
    {
        // Hämtar det nuvarande talet man skrev in.
        double v = Double.parseDouble(this.botValue.toString());

        // Om talet är 0
        if (v == 0)
        {
            // Är talet 0 så fryser man miniräknaren så att användaren inte kan göra något.
            // Ett meddelande visas och frysningen tas bort genom att trycka på C(Clear) eller CE(Clear Entry)
            // Fryser räknaren så att man inte kan göra något. Tas bort genom att trycka Clear eller ClearEntry.
            this.freeze = true;
            this.botValueTxt.setText("Det går inte att dela med noll.");

            return;
        }
        else
        {
            // Räkna
            this.calculation /= v;
            // Ta bort allt i botValue.
            this.botValue.delete(0, this.botValue.length());
            // Lägg till svaret i botValue.
            this.botValue.append(this.calculation);
            // Uppdatera botValueTxt.
            this.botValueTxt.setText(this.botValue.toString());
        }
    }

    /**
     * Metod som laddar den senaste valutan från en XML fil.
     */
    private void loadCurrency()
    {
        try {
            // Skapar en url av länken.
            URL url = new URL("http://www.ecb.int/stats/eurofxref/eurofxref-daily.xml");

            // Skapar ett XML document.
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(url.openStream());

            // Hämtar en nodelista med element med namn time och location.
            NodeList cubeList = doc.getElementsByTagName("Cube");

            this.currency.clear();

            for (int i = 2; i < cubeList.getLength(); i++)
            {
                NamedNodeMap cube = cubeList.item(i).getAttributes();

                String currency = cube.getNamedItem("currency").getTextContent();

                this.currencyFrom.addItem(currency);
                this.currencyTo.addItem(currency);

                if (currency.equalsIgnoreCase("SEK"))
                    this.currencyFrom.setSelectedIndex(this.currencyFrom.getItemCount() - 1);

                this.currency.put(currency, Double.parseDouble(cube.getNamedItem("rate").getTextContent()));
            }

            this.currencyFrom.addItem("EUR");
            this.currencyTo.addItem("EUR");
            this.currency.put("EUR", 1.0);

        } catch (Exception e) { }
    }

    /**
     * Creates new form Calculator
     */
    public Calculator() {
        initComponents();

        this.botValue = new StringBuffer("0.0");
        this.botValueTxt.setText("0.0");
        this.topValueTxt.setText("");

        this.newNumber = true;
        this.calculation = 0.0;
        this.initializedCalculations = false;
        this.freeze = false;
        this.comma = false;
        this.latestOperator = Symbols.Digit;

        this.oneBtn.addActionListener(this);
        this.twoBtn.addActionListener(this);
        this.threeBtn.addActionListener(this);

        this.fourBtn.addActionListener(this);
        this.fiveBtn.addActionListener(this);
        this.sixBtn.addActionListener(this);

        this.sevenBtn.addActionListener(this);
        this.eightBtn.addActionListener(this);
        this.nineBtn.addActionListener(this);

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);

        this.loadCurrency();

        this.setVisible(true);
    }

    /**
     * Metod för att initializera formuläret.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        oneBtn = new javax.swing.JButton();
        twoBtn = new javax.swing.JButton();
        threeBtn = new javax.swing.JButton();
        fourBtn = new javax.swing.JButton();
        fiveBtn = new javax.swing.JButton();
        sixBtn = new javax.swing.JButton();
        sevenBtn = new javax.swing.JButton();
        eightBtn = new javax.swing.JButton();
        nineBtn = new javax.swing.JButton();
        zeroBtn = new javax.swing.JButton();
        commaBtn = new javax.swing.JButton();
        addBtn = new javax.swing.JButton();
        subBtn = new javax.swing.JButton();
        multiplyBtn = new javax.swing.JButton();
        divideBtn = new javax.swing.JButton();
        clearBtn = new javax.swing.JButton();
        percentBtn = new javax.swing.JButton();
        oneDividedByXBtn = new javax.swing.JButton();
        rootOfBtn = new javax.swing.JButton();
        plusMinusBtn = new javax.swing.JButton();
        backBtn = new javax.swing.JButton();
        clearEntryBtn = new javax.swing.JButton();
        memoryMinusBtn = new javax.swing.JButton();
        memoryPlusBtn = new javax.swing.JButton();
        memorySave = new javax.swing.JButton();
        memoryResumeBtn = new javax.swing.JButton();
        memoryClearBtn = new javax.swing.JButton();
        equalBtn = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        botValueTxt = new javax.swing.JTextField();
        topValueTxt = new javax.swing.JTextField();
        memoryTxt = new javax.swing.JTextField();
        currencyTo = new javax.swing.JComboBox();
        convertBtn = new javax.swing.JButton();
        currencyFrom = new javax.swing.JComboBox();

        setBackground(new java.awt.Color(255, 255, 255));
        setOpaque(false);

        oneBtn.setText("1");
        oneBtn.setIconTextGap(0);

        twoBtn.setText("2");
        twoBtn.setIconTextGap(0);

        threeBtn.setText("3");
        threeBtn.setIconTextGap(0);
        threeBtn.setMaximumSize(new java.awt.Dimension(45, 23));
        threeBtn.setMinimumSize(new java.awt.Dimension(45, 23));

        fourBtn.setText("4");
        fourBtn.setIconTextGap(0);

        fiveBtn.setText("5");
        fiveBtn.setIconTextGap(0);

        sixBtn.setText("6");
        sixBtn.setIconTextGap(0);

        sevenBtn.setText("7");
        sevenBtn.setIconTextGap(0);

        eightBtn.setText("8");
        eightBtn.setIconTextGap(0);

        nineBtn.setText("9");
        nineBtn.setIconTextGap(0);

        zeroBtn.setText("0");
        zeroBtn.setIconTextGap(0);
        zeroBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zeroBtnActionPerformed(evt);
            }
        });

        commaBtn.setText(",");
        commaBtn.setIconTextGap(0);
        commaBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                commaBtnActionPerformed(evt);
            }
        });

        addBtn.setText("+");
        addBtn.setIconTextGap(0);
        addBtn.setMargin(new java.awt.Insets(0, 0, 0, 0));
        addBtn.setMaximumSize(new java.awt.Dimension(40, 23));
        addBtn.setMinimumSize(new java.awt.Dimension(40, 23));
        addBtn.setPreferredSize(new java.awt.Dimension(40, 23));
        addBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addBtnActionPerformed(evt);
            }
        });

        subBtn.setText("-");
        subBtn.setIconTextGap(0);
        subBtn.setMaximumSize(new java.awt.Dimension(40, 23));
        subBtn.setMinimumSize(new java.awt.Dimension(40, 23));
        subBtn.setPreferredSize(new java.awt.Dimension(40, 23));
        subBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                subBtnActionPerformed(evt);
            }
        });

        multiplyBtn.setText("*");
        multiplyBtn.setIconTextGap(0);
        multiplyBtn.setMaximumSize(new java.awt.Dimension(40, 23));
        multiplyBtn.setMinimumSize(new java.awt.Dimension(40, 23));
        multiplyBtn.setPreferredSize(new java.awt.Dimension(40, 23));
        multiplyBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                multiplyBtnActionPerformed(evt);
            }
        });

        divideBtn.setText("/");
        divideBtn.setIconTextGap(0);
        divideBtn.setMaximumSize(new java.awt.Dimension(40, 23));
        divideBtn.setMinimumSize(new java.awt.Dimension(40, 23));
        divideBtn.setPreferredSize(new java.awt.Dimension(40, 23));
        divideBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                divideBtnActionPerformed(evt);
            }
        });

        clearBtn.setText("C");
        clearBtn.setMargin(new java.awt.Insets(0, 0, 0, 0));
        clearBtn.setMaximumSize(new java.awt.Dimension(40, 23));
        clearBtn.setMinimumSize(new java.awt.Dimension(40, 23));
        clearBtn.setPreferredSize(new java.awt.Dimension(40, 23));
        clearBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearBtnActionPerformed(evt);
            }
        });

        percentBtn.setText("%");
        percentBtn.setIconTextGap(0);
        percentBtn.setMargin(new java.awt.Insets(0, 0, 0, 0));
        percentBtn.setMaximumSize(new java.awt.Dimension(40, 23));
        percentBtn.setMinimumSize(new java.awt.Dimension(40, 23));
        percentBtn.setPreferredSize(new java.awt.Dimension(40, 23));
        percentBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                percentBtnActionPerformed(evt);
            }
        });

        oneDividedByXBtn.setText("1/x");
        oneDividedByXBtn.setIconTextGap(0);
        oneDividedByXBtn.setInheritsPopupMenu(true);
        oneDividedByXBtn.setMargin(new java.awt.Insets(0, 0, 0, 0));
        oneDividedByXBtn.setMaximumSize(new java.awt.Dimension(40, 23));
        oneDividedByXBtn.setMinimumSize(new java.awt.Dimension(40, 23));
        oneDividedByXBtn.setPreferredSize(new java.awt.Dimension(40, 23));
        oneDividedByXBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                oneDividedByXBtnActionPerformed(evt);
            }
        });

        rootOfBtn.setText("√");
        rootOfBtn.setMargin(new java.awt.Insets(0, 0, 0, 0));
        rootOfBtn.setMaximumSize(new java.awt.Dimension(40, 23));
        rootOfBtn.setMinimumSize(new java.awt.Dimension(40, 23));
        rootOfBtn.setPreferredSize(new java.awt.Dimension(40, 23));
        rootOfBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rootOfBtnActionPerformed(evt);
            }
        });

        plusMinusBtn.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        plusMinusBtn.setText("±");
        plusMinusBtn.setMargin(new java.awt.Insets(0, 0, 0, 0));
        plusMinusBtn.setMaximumSize(new java.awt.Dimension(40, 23));
        plusMinusBtn.setMinimumSize(new java.awt.Dimension(40, 23));
        plusMinusBtn.setPreferredSize(new java.awt.Dimension(40, 23));
        plusMinusBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                plusMinusBtnActionPerformed(evt);
            }
        });

        backBtn.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        backBtn.setText("←");
        backBtn.setIconTextGap(0);
        backBtn.setMargin(new java.awt.Insets(-2, -2, 2, -2));
        backBtn.setMaximumSize(new java.awt.Dimension(40, 23));
        backBtn.setMinimumSize(new java.awt.Dimension(40, 23));
        backBtn.setPreferredSize(new java.awt.Dimension(40, 23));
        backBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backBtnActionPerformed(evt);
            }
        });

        clearEntryBtn.setText("CE");
        clearEntryBtn.setMargin(new java.awt.Insets(0, 0, 0, 0));
        clearEntryBtn.setMaximumSize(new java.awt.Dimension(40, 23));
        clearEntryBtn.setMinimumSize(new java.awt.Dimension(40, 23));
        clearEntryBtn.setPreferredSize(new java.awt.Dimension(40, 23));
        clearEntryBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearEntryBtnActionPerformed(evt);
            }
        });

        memoryMinusBtn.setText("M-");
        memoryMinusBtn.setIconTextGap(0);
        memoryMinusBtn.setMargin(new java.awt.Insets(0, 0, 0, 0));
        memoryMinusBtn.setMaximumSize(new java.awt.Dimension(40, 23));
        memoryMinusBtn.setMinimumSize(new java.awt.Dimension(40, 23));
        memoryMinusBtn.setPreferredSize(new java.awt.Dimension(40, 23));
        memoryMinusBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                memoryMinusBtnActionPerformed(evt);
            }
        });

        memoryPlusBtn.setText("M+");
        memoryPlusBtn.setMargin(new java.awt.Insets(0, 0, 0, 0));
        memoryPlusBtn.setMaximumSize(new java.awt.Dimension(40, 23));
        memoryPlusBtn.setMinimumSize(new java.awt.Dimension(40, 23));
        memoryPlusBtn.setPreferredSize(new java.awt.Dimension(40, 23));
        memoryPlusBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                memoryPlusBtnActionPerformed(evt);
            }
        });

        memorySave.setText("MS");
        memorySave.setMargin(new java.awt.Insets(0, 0, 0, 0));
        memorySave.setMaximumSize(new java.awt.Dimension(40, 23));
        memorySave.setMinimumSize(new java.awt.Dimension(40, 23));
        memorySave.setPreferredSize(new java.awt.Dimension(40, 23));
        memorySave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                memorySaveActionPerformed(evt);
            }
        });

        memoryResumeBtn.setText("MR");
        memoryResumeBtn.setMargin(new java.awt.Insets(0, 0, 0, 0));
        memoryResumeBtn.setMaximumSize(new java.awt.Dimension(40, 23));
        memoryResumeBtn.setMinimumSize(new java.awt.Dimension(40, 23));
        memoryResumeBtn.setPreferredSize(new java.awt.Dimension(40, 23));
        memoryResumeBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                memoryResumeBtnActionPerformed(evt);
            }
        });

        memoryClearBtn.setText("MC");
        memoryClearBtn.setMargin(new java.awt.Insets(0, 0, 0, 0));
        memoryClearBtn.setMaximumSize(new java.awt.Dimension(40, 23));
        memoryClearBtn.setMinimumSize(new java.awt.Dimension(40, 23));
        memoryClearBtn.setPreferredSize(new java.awt.Dimension(40, 23));
        memoryClearBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                memoryClearBtnActionPerformed(evt);
            }
        });

        equalBtn.setText("=");
        equalBtn.setIconTextGap(0);
        equalBtn.setMargin(new java.awt.Insets(0, 0, 0, 0));
        equalBtn.setMaximumSize(new java.awt.Dimension(40, 23));
        equalBtn.setMinimumSize(new java.awt.Dimension(40, 23));
        equalBtn.setPreferredSize(new java.awt.Dimension(40, 23));
        equalBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                equalBtnActionPerformed(evt);
            }
        });

        jPanel1.setBackground(new java.awt.Color(239, 246, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(102, 102, 102)));

        botValueTxt.setEditable(false);
        botValueTxt.setBackground(new java.awt.Color(239, 246, 255));
        botValueTxt.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        botValueTxt.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        botValueTxt.setText("0.0");
        botValueTxt.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        botValueTxt.setMargin(new java.awt.Insets(0, 0, 0, 0));

        topValueTxt.setEditable(false);
        topValueTxt.setBackground(new java.awt.Color(239, 246, 255));
        topValueTxt.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        topValueTxt.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        topValueTxt.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        memoryTxt.setEditable(false);
        memoryTxt.setBackground(new java.awt.Color(239, 246, 255));
        memoryTxt.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        memoryTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        memoryTxt.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(topValueTxt)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(memoryTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(botValueTxt)))
                .addGap(0, 0, 0))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(topValueTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(botValueTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(memoryTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        currencyTo.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N

        convertBtn.setText("Currency");
        convertBtn.setIconTextGap(0);
        convertBtn.setMargin(new java.awt.Insets(0, 0, 0, 0));
        convertBtn.setMaximumSize(new java.awt.Dimension(40, 23));
        convertBtn.setMinimumSize(new java.awt.Dimension(40, 23));
        convertBtn.setPreferredSize(new java.awt.Dimension(40, 23));
        convertBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                convertBtnActionPerformed(evt);
            }
        });

        currencyFrom.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(memoryClearBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(memoryResumeBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(memorySave, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(memoryPlusBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(memoryMinusBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(currencyFrom, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(12, 12, 12)
                            .addComponent(currencyTo, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(convertBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                    .addComponent(fourBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(fiveBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 33, Short.MAX_VALUE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(sixBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(multiplyBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(oneDividedByXBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(backBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(sevenBtn))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                            .addComponent(clearEntryBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(clearBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(plusMinusBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(rootOfBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                            .addComponent(eightBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(nineBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 28, Short.MAX_VALUE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(divideBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(percentBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addComponent(zeroBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(commaBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(addBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addComponent(oneBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(twoBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(threeBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 28, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(subBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(equalBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))))))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {addBtn, backBtn, clearBtn, clearEntryBtn, commaBtn, divideBtn, eightBtn, equalBtn, fiveBtn, fourBtn, memoryClearBtn, memoryMinusBtn, memoryPlusBtn, memoryResumeBtn, memorySave, multiplyBtn, nineBtn, oneBtn, oneDividedByXBtn, percentBtn, plusMinusBtn, rootOfBtn, sevenBtn, sixBtn, subBtn, threeBtn, twoBtn});

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {currencyFrom, currencyTo});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(memorySave, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(memoryResumeBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(memoryClearBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(memoryPlusBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(memoryMinusBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(rootOfBtn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(clearBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(clearEntryBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(plusMinusBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(sevenBtn)
                            .addComponent(eightBtn)
                            .addComponent(nineBtn)
                            .addComponent(percentBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(divideBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(oneDividedByXBtn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(fourBtn)
                                .addComponent(fiveBtn)
                                .addComponent(sixBtn)
                                .addComponent(multiplyBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(oneBtn)
                                    .addComponent(twoBtn)
                                    .addComponent(threeBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(subBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(zeroBtn)
                                    .addComponent(commaBtn)
                                    .addComponent(addBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(equalBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(currencyFrom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(currencyTo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(convertBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(backBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {subBtn, threeBtn});

    }// </editor-fold>//GEN-END:initComponents

    /**
     * Metod som körs varje gång användaren trycker på 0.
     * Metoden lägger till en nolla.
     * @param evt ActionEventet som skickas med.
     */
    private void zeroBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zeroBtnActionPerformed
        // Om freeze är true ska inget hända.
        if (freeze)
            return;

        // Om det inte är första siffran så får man skriva en nolla.
        if (!newNumber)
        {
            if (this.comma || this.botValue.indexOf(".") == -1)
                this.botValue.append("0");
            else
                this.botValue.insert(this.botValue.indexOf("."), "0"); // Vi lägger till en nolla precis före kommatecknet i botValue.

            this.botValueTxt.setText(this.botValue.toString()); // Vi uppdaterar botValueTxt till senaste botValue texten.
        }
        else
        {
            // Om det är första siffran och inte en nolla redan finns får man skriva
            if(!botValueTxt.toString().startsWith("0"))
            {
                this.botValue.delete(0, this.botValue.length()); // Vi tar bort allt i botValue.
                this.botValue.append("0.0"); // Vi lägger till en 0a.
                this.botValueTxt.setText(this.botValue.toString()); // Uppdaterar botValueTxt texten.

                // Vi sätter inte newNum till false då man inte ska kunna trycka 0 sen 2
                // utan vi måste trycka , först. Så newNum sätts till false först vid , knappen.
            }
        }

        this.latestButton = Symbols.Digit; // Ändrar senaste knappen til digit.
    }//GEN-LAST:event_zeroBtnActionPerformed

    /**
     * Metod som körs varje gång användaren trycker på kommatecknet.
     * Metoden lägger till ett kommatecken.
     * @param evt ActionEventet som skickas med.
     */
    private void commaBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_commaBtnActionPerformed
        // Om freeze är true ska inget hända.
        if (freeze)
            return;

        // Har vi redan använt kommatecken så ska man inte kunna göra ett till.
        if (this.comma)
            return;

        this.comma = true; // Sätt comma till true.

        // Om kommatecknet finns så tar vi bort allt efter kommatecknet.
        if (this.botValue.indexOf(".") > -1)
            this.botValue.delete(this.botValue.indexOf(".") + 1, this.botValue.length()); // Ta bort allt efter kommatecknet.
        else // Om kommatecknet inte finns så lägger vi till ett kommatecken.
            this.botValue.append(".");

        this.botValueTxt.setText(this.botValue.toString());
    }//GEN-LAST:event_commaBtnActionPerformed

    /**
     * Metod som körs varje gång användaren trycker på backspace.
     * Metoden tar bort en siffra du skrivit in.
     * @param evt ActionEventet som skickas med.
     */
    private void backBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backBtnActionPerformed
        // Om freeze är true ska inget hända.
        if (freeze)
            return;

        // Har vi satt ett kommatecken.
        if (this.comma)
        {
            // Om tecknet vi ska ta bort är ett kommatecken så sätt this.comma till false.
            if (this.botValue.charAt(this.botValue.length() - 1) == '.')
                this.comma = false;

            // Ta bort det sista tecknet.
            this.botValue.delete(this.botValue.length() - 1, this.botValue.length());
        }
        else
        {
            // Om vi inte satt ett kommatecken.

            // Om kommatecknet finns ändå.
            if (this.botValue.indexOf(".") > 0)
            {
                // Så tar vi bort siffran före kommatecknet.
                this.botValue.delete(this.botValue.indexOf(".") - 1, this.botValue.indexOf("."));

                // Om kommatecknet är det första talet.
                if (this.botValue.indexOf(".") == 0)
                {
                    // Så tar vi bort hela botValue.
                    this.botValue.delete(0, this.botValue.length());
                    // lägger till 0.0 i botValue.
                    this.botValue.append("0.0");
                    // Nästa siffra ska börja ett nytt tal.
                    this.newNumber = true;
                }
            }
            else if (this.botValue.indexOf(".") == -1 && this.botValue.length() > 0) // Om kommatecknet inte finns och botValue inte är tom.
            {
                // Så tar vi bort det senaste tecknet.
                this.botValue.delete(this.botValue.length() - 1, this.botValue.length());

                // Om botValue är tom efter man tagit bort tecknet
                if (this.botValue.length() == 0)
                {
                    // Så lägger vi till 0.0 i botValue
                    this.botValue.append("0.0");
                    // Nästa siffra ska börja ett nytt tal.
                    this.newNumber = true;
                }
            }
        }

        this.botValueTxt.setText(this.botValue.toString()); // Uppdatera botValueTxt texten.
    }//GEN-LAST:event_backBtnActionPerformed

    /**
     * Metod som körs varje gång användaren trycker på clearEntry.
     * Metoden tar bort allt du precis skrev in.
     * @param evt ActionEventet som skickas med.
     */
    private void clearEntryBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearEntryBtnActionPerformed
        // Vi resetar alla variabler som har med nuvarande talet att göra.
        this.newNumber = true;
        this.botValue.delete(0, this.botValue.length());
        this.botValue.append("0.0");
        this.botValueTxt.setText("0.0");
        this.freeze = false;
        this.comma = false;
        this.latestButton = this.latestOperator;
    }//GEN-LAST:event_clearEntryBtnActionPerformed

    /**
     * Metod som körs varje gång användaren trycker på clear.
     * Metoden tar bort allt.
     * @param evt ActionEventet som skickas med.
     */
    private void clearBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearBtnActionPerformed
        // Vi resetar alla variabler.
        this.newNumber = true;
        this.calculation = 0.0;
        this.botValue.delete(0, this.botValue.length());
        this.botValue.append("0.0");
        this.botValueTxt.setText("0.0");
        this.topValueTxt.setText("");
        this.initializedCalculations = false;
        this.freeze = false;
        this.comma = false;
        this.latestOperator = Symbols.Digit;
    }//GEN-LAST:event_clearBtnActionPerformed

    /**
     * Metod som körs varje gång användaren trycker på plus/minus tecknet..
     * Metoden gör om ett positivt tal till negativt och tvärt om.
     * @param evt ActionEventet som skickas med.
     */
    private void plusMinusBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_plusMinusBtnActionPerformed
        // Om freeze är true ska inget hända.
        if (freeze)
            return;

        if (this.botValue.substring(0, 1).equalsIgnoreCase("-")) // Om första tecknet är ett minustecken.
            this.botValue.delete(0, 1); // Så tar vi bort minustecknet.
        else // Om inte första tecknet är ett minustecken så lägger vi till ett minustecken i början av texten.
            this.botValue.insert(0, "-");

        this.botValueTxt.setText(this.botValue.toString());
    }//GEN-LAST:event_plusMinusBtnActionPerformed

    /**
     * Metod som körs varje gång användaren trycker på addera.
     * Metoden adderar.
     * @param evt ActionEventet som skickas med.
     */
    private void addBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addBtnActionPerformed
        // När man trycker på en operator så ska nästa tal man skriver in börja på nytt.
        this.newNumber = true;
        // Vi har inte skrivit ett kommatecken längre.
        this.comma = false;

        // Om senaste knappen man tryckte på var + ska inget hända.
        if (this.latestButton != Symbols.Add)
        {
            // Om topValueTxt är tom så ska man inte räkna utan bara sätta calculation till botValue och ändra topValueTxt.
            if (this.topValueTxt.getText().trim().isEmpty())
            {
                this.calculation = Double.parseDouble(this.botValue.toString());
                this.topValueTxt.setText(this.calculation + " + ");
            }
            else if (this.topValueTxt.getText().substring(this.topValueTxt.getText().length() - 1).equalsIgnoreCase(")"))
            {
                this.topValueTxt.setText(this.topValueTxt.getText() + " + ");
                this.botValue.delete(0, this.botValue.length());
                this.botValue.append(this.calculation);
                this.botValueTxt.setText(this.botValue.toString());
            }
            else
            {
                // Om topValueTxt inte är tomt
                if (this.latestButton == Symbols.Digit)
                {
                    if (this.latestOperator == Symbols.Add)
                    {
                        // Vi ska addera så ändra topValue och addera.
                        this.topValueTxt.setText(this.topValueTxt.getText() + this.botValue.toString() + " + "); // Ändra topValueTxt.
                        this.Add();
                    }
                    else if (this.latestOperator == Symbols.Sub)
                    {
                        // Vi ska subtrahera så ändra topValue och subtrahera.
                        this.topValueTxt.setText(this.topValueTxt.getText() + this.botValue.toString() + " + "); // Ändra topValueTxt.
                        this.Sub();
                    }
                    else if (this.latestOperator == Symbols.Multiply)
                    {
                        // Vi ska multiplicera så ändra topValue och multiplicera.
                        this.topValueTxt.setText(this.topValueTxt.getText() + this.botValue.toString() + " + "); // Ändra topValueTxt.
                        this.Multiply();
                    }
                    else if (this.latestOperator == Symbols.Divide)
                    {
                        // Vi ska dividera så ändra topValue och dividera.
                        this.topValueTxt.setText(this.topValueTxt.getText() + this.botValue.toString() + " + "); // Ändra topValueTxt.
                        this.Divide();
                    }
                }
                else
                { // Om senaste knappen inte var en siffra så ändrar vi tecken.
                    // om sista tecknet är en ) så ska vi lägga till " + "
                    if (this.topValueTxt.getText().substring(this.topValueTxt.getText().length() - 1).equalsIgnoreCase(")"))
                        this.topValueTxt.setText(this.topValueTxt.getText() + " + ");
                    else // Om sista tecknet inte är en ) så ändrar vi tecken.
                        this.topValueTxt.setText(this.topValueTxt.getText().substring(0, this.topValueTxt.getText().length() - 2) + "+ ");
                }
            }

            this.latestOperator = Symbols.Add;
            this.latestButton = Symbols.Add;
        }
    }//GEN-LAST:event_addBtnActionPerformed

    /**
     * Metod som körs varje gång användaren trycker på subtrahera.
     * Metoden subtraherar.
     * @param evt ActionEventet som skickas med.
     */
    private void subBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_subBtnActionPerformed
        // När man trycker på en operator så ska nästa tal man skriver in börja på nytt.
        this.newNumber = true;
        // Vi har inte skrivit ett kommatecken längre.
        this.comma = false;

        // Om senaste knappen man tryckte på var - ska inget hända.
        if (this.latestButton != Symbols.Sub)
        {
            // Om topValueTxt är tom så ska man inte räkna utan bara sätta calculation till botValue och ändra topValueTxt.
            if (this.topValueTxt.getText().trim().isEmpty())
            {
                this.calculation = Double.parseDouble(this.botValue.toString());
                this.topValueTxt.setText(this.calculation + " - ");
            }
            else if (this.topValueTxt.getText().substring(this.topValueTxt.getText().length() - 1).equalsIgnoreCase(")"))
            {
                // Om senaste bokstaven var ett ) så ska man bara lägga till ett -
                this.topValueTxt.setText(this.topValueTxt.getText() + " - ");
                this.botValue.delete(0, this.botValue.length());
                this.botValue.append(this.calculation);
                this.botValueTxt.setText(this.botValue.toString());
            }
            else
            {
                // Om topValueTxt inte är tomt
                if (this.latestButton == Symbols.Digit)
                {
                    if (this.latestOperator == Symbols.Add)
                    {
                        // Vi ska addera så ändra topValue och addera.
                        this.topValueTxt.setText(this.topValueTxt.getText() + this.botValue.toString() + " - "); // Ändra topValueTxt.
                        this.Add();
                    }
                    else if (this.latestOperator == Symbols.Sub)
                    {
                        // Vi ska subtrahera så ändra topValue och subtrahera.
                        this.topValueTxt.setText(this.topValueTxt.getText() + this.botValue.toString() + " - "); // Ändra topValueTxt.
                        this.Sub();
                    }
                    else if (this.latestOperator == Symbols.Multiply)
                    {
                        // Vi ska multiplicera så ändra topValue och multiplicera.
                        this.topValueTxt.setText(this.topValueTxt.getText() + this.botValue.toString() + " - "); // Ändra topValueTxt.
                        this.Multiply();
                    }
                    else if (this.latestOperator == Symbols.Divide)
                    {
                        // Vi ska dividera så ändra topValue och dividera.
                        this.topValueTxt.setText(this.topValueTxt.getText() + this.botValue.toString() + " - "); // Ändra topValueTxt.
                        this.Divide();
                    }
                }
                else
                { // Om senaste knappen inte var en siffra så ändrar vi tecken.
                    // om sista tecknet är en ) så ska vi lägga till " + "
                    if (this.topValueTxt.getText().substring(this.topValueTxt.getText().length() - 1).equalsIgnoreCase(")"))
                        this.topValueTxt.setText(this.topValueTxt.getText() + " - ");
                    else // Om sista tecknet inte är en ) så ändrar vi tecken.
                        this.topValueTxt.setText(this.topValueTxt.getText().substring(0, this.topValueTxt.getText().length() - 2) + "- ");
                }
            }

            this.latestOperator = Symbols.Sub;
            this.latestButton = Symbols.Sub;
        }
    }//GEN-LAST:event_subBtnActionPerformed

    /**
     * Metod som körs varje gång användaren trycker på multiplicera.
     * Metoden räknar ut multiplicaiton.
     * @param evt ActionEventet som skickas med.
     */
    private void multiplyBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_multiplyBtnActionPerformed
        // När man trycker på en operator så ska nästa tal man skriver in börja på nytt.
        this.newNumber = true;
        // Vi har inte skrivit ett kommatecken längre.
        this.comma = false;

        // Om senaste knappen man tryckte på var * ska inget hända.
        if (this.latestButton != Symbols.Multiply)
        {
            // Om topValueTxt är tom så ska man inte räkna utan bara sätta calculation till botValue och ändra topValueTxt.
            if (this.topValueTxt.getText().trim().isEmpty())
            {
                this.calculation = Double.parseDouble(this.botValue.toString());
                this.topValueTxt.setText(this.calculation + " * ");
            }
            else if (this.topValueTxt.getText().substring(this.topValueTxt.getText().length() - 1).equalsIgnoreCase(")"))
            {
                // Om senaste bokstaven var ett ) så ska man bara lägga till ett -
                this.topValueTxt.setText(this.topValueTxt.getText() + " * ");
                this.botValue.delete(0, this.botValue.length());
                this.botValue.append(this.calculation);
                this.botValueTxt.setText(this.botValue.toString());
            }
            else
            {
                // Om topValueTxt inte är tomt
                if (this.latestButton == Symbols.Digit)
                {
                    if (this.latestOperator == Symbols.Add)
                    {
                        // Vi ska addera så ändra topValue och addera.
                        this.topValueTxt.setText(this.topValueTxt.getText() + this.botValue.toString() + " * "); // Ändra topValueTxt.
                        this.Add();
                    }
                    else if (this.latestOperator == Symbols.Sub)
                    {
                        // Vi ska subtrahera så ändra topValue och subtrahera.
                        this.topValueTxt.setText(this.topValueTxt.getText() + this.botValue.toString() + " * "); // Ändra topValueTxt.
                        this.Sub();
                    }
                    else if (this.latestOperator == Symbols.Multiply)
                    {
                        // Vi ska multiplicera så ändra topValue och multiplicera.
                        this.topValueTxt.setText(this.topValueTxt.getText() + this.botValue.toString() + " * "); // Ändra topValueTxt.
                        this.Multiply();
                    }
                    else if (this.latestOperator == Symbols.Divide)
                    {
                        // Vi ska dividera så ändra topValue och dividera.
                        this.topValueTxt.setText(this.topValueTxt.getText() + this.botValue.toString() + " * "); // Ändra topValueTxt.
                        this.Divide();
                    }
                }
                else
                { // Om senaste knappen inte var en siffra så ändrar vi tecken.
                    // om sista tecknet är en ) så ska vi lägga till " + "
                    if (this.topValueTxt.getText().substring(this.topValueTxt.getText().length() - 1).equalsIgnoreCase(")"))
                        this.topValueTxt.setText(this.topValueTxt.getText() + " * ");
                    else // Om sista tecknet inte är en ) så ändrar vi tecken.
                        this.topValueTxt.setText(this.topValueTxt.getText().substring(0, this.topValueTxt.getText().length() - 2) + "* ");
                }
            }

            this.latestOperator = Symbols.Multiply;
            this.latestButton = Symbols.Multiply;
        }
    }//GEN-LAST:event_multiplyBtnActionPerformed

    /**
     * Metod som körs varje gång användaren trycker på division.
     * Metoden räknar ut divison.
     * @param evt ActionEventet som skickas med.
     */
    private void divideBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_divideBtnActionPerformed
        // När man trycker på en operator så ska nästa tal man skriver in börja på nytt.
        this.newNumber = true;
        // Vi har inte skrivit ett kommatecken längre.
        this.comma = false;

        // Om senaste knappen man tryckte på var * ska inget hända.
        if (this.latestButton != Symbols.Divide)
        {
            // Om topValueTxt är tom så ska man inte räkna utan bara sätta calculation till botValue och ändra topValueTxt.
            if (this.topValueTxt.getText().trim().isEmpty())
            {
                this.calculation = Double.parseDouble(this.botValue.toString());
                this.topValueTxt.setText(this.calculation + " / ");
            }
            else if (this.topValueTxt.getText().substring(this.topValueTxt.getText().length() - 1).equalsIgnoreCase(")"))
            {
                // Om senaste bokstaven var ett ) så ska man bara lägga till ett -
                this.topValueTxt.setText(this.topValueTxt.getText() + " / ");
                this.botValue.delete(0, this.botValue.length());
                this.botValue.append(this.calculation);
                this.botValueTxt.setText(this.botValue.toString());
            }
            else
            {
                // Om topValueTxt inte är tomt
                if (this.latestButton == Symbols.Digit)
                {
                    if (this.latestOperator == Symbols.Add)
                    {
                        // Vi ska addera så ändra topValue och addera.
                        this.topValueTxt.setText(this.topValueTxt.getText() + this.botValue.toString() + " / "); // Ändra topValueTxt.
                        this.Add();
                    }
                    else if (this.latestOperator == Symbols.Sub)
                    {
                        // Vi ska subtrahera så ändra topValue och subtrahera.
                        this.topValueTxt.setText(this.topValueTxt.getText() + this.botValue.toString() + " / "); // Ändra topValueTxt.
                        this.Sub();
                    }
                    else if (this.latestOperator == Symbols.Multiply)
                    {
                        // Vi ska multiplicera så ändra topValue och multiplicera.
                        this.topValueTxt.setText(this.topValueTxt.getText() + this.botValue.toString() + " / "); // Ändra topValueTxt.
                        this.Multiply();
                    }
                    else if (this.latestOperator == Symbols.Divide)
                    {
                        // Vi ska dividera så ändra topValue och dividera.
                        this.topValueTxt.setText(this.topValueTxt.getText() + this.botValue.toString() + " / "); // Ändra topValueTxt.
                        this.Divide();
                    }
                }
                else
                { // Om senaste knappen inte var en siffra så ändrar vi tecken.
                    // om sista tecknet är en ) så ska vi lägga till " + "
                    if (this.topValueTxt.getText().substring(this.topValueTxt.getText().length() - 1).equalsIgnoreCase(")"))
                        this.topValueTxt.setText(this.topValueTxt.getText() + " / ");
                    else // Om sista tecknet inte är en ) så ändrar vi tecken.
                        this.topValueTxt.setText(this.topValueTxt.getText().substring(0, this.topValueTxt.getText().length() - 2) + "/ ");
                }
            }

            this.latestOperator = Symbols.Divide;
            this.latestButton = Symbols.Divide;
        }
    }//GEN-LAST:event_divideBtnActionPerformed

    /**
     * Metod som körs varje gång användaren trycker på enter.
     * Metoden räknar ut och ger ett resultat. Även alla ICalcEventListeners
     * calculationDone metoder körs.
     * @param evt ActionEventet som skickas med.
     */
    private void equalBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_equalBtnActionPerformed
        if (this.latestOperator == Symbols.Add)
        {
            if (this.latestButton == Symbols.Digit)
            {
                // Om senaste operatorn var Add så ska vi addrea.
                this.calculation += Double.parseDouble(this.botValue.toString());
            }
        }
        else if (this.latestOperator == Symbols.Sub)
        {
            if (this.latestButton == Symbols.Digit)
            {
                // Om senaste operatorn var Sub så ska vi subtrahera.
                this.calculation -= Double.parseDouble(this.botValue.toString());
            }
        }
        else if (this.latestOperator == Symbols.Multiply)
        {
            if (this.latestButton == Symbols.Digit)
            {
                // Om senaste operatorn var Multiply så ska vi multiplicera.
                this.calculation *= Double.parseDouble(this.botValue.toString());
            }
        }
        else if (this.latestOperator == Symbols.Divide)
        {
            if (this.latestButton == Symbols.Digit)
            {
                // Om senaste operatorn var Divide så ska vi dividera.
                double v = Double.parseDouble(this.botValue.toString());

                if (v == 0)
                {
                    // Fryser räknaren så att man inte kan göra något. Tas bort genom att trycka C eller CE.
                    this.freeze = true;
                    this.botValueTxt.setText("Det går inte att dela med noll.");
                    return;
                }
                else
                {
                    this.calculation /= v;
                }
            }
        }

        // Om senaste operatorn inte var en digit, dvs denna kommer alltid vara true förutom när man precis
        // startat eller om man tryckt på Clear eller Clear Entry.
        if (this.latestOperator != Symbols.Digit)
            this.initializedCalculations = true;

        // Om initializedCalculations är true betyder det att vi inte är vid första steget längre
        // och ska skriva ut uträkningen på skärmen.
        if (this.initializedCalculations)
        {
            // Har vi räknat ut något så visa uträkningen.
            this.botValue.delete(0, this.botValue.length());
            this.botValue.append(this.calculation);
            this.botValueTxt.setText(this.botValue.toString());
        }
        else
        {
            // Om initializedCalculations är false så betyder det att vi fortfarande är vid första steget
            // och ska bara skriva ut talet.
            this.botValueTxt.setText(this.botValue.toString());
        }

        this.topValueTxt.setText(""); // Ta bort topValueTxt.
        this.newNumber = true; // Nästa digit ska börja ett nytt tal.
        this.comma = false; // Vi har inte skrivit ett kommatecken längre.
        this.latestButton = Symbols.Digit; // Senaste knappen var en digit.
        this.latestOperator = Symbols.Digit; // Senaste operatorn var en digit.
        this.fireCalcEvent();
    }//GEN-LAST:event_equalBtnActionPerformed

    /**
     * Metod som körs varje gång användaren trycker på 1/X tecknet.
     * Metoden dividerar 1 med det talet man skrivit in.
     * @param evt ActionEventet som skickas med.
     */
    private void oneDividedByXBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_oneDividedByXBtnActionPerformed
        // När man trycker på en operator så ska nästa tal man skriver in börja på nytt.
        this.newNumber = true;
        // Vi har inte skrivit ett kommatecken längre.
        this.comma = false;

        double tmp = Double.parseDouble(this.botValue.toString());

        // Om topValueTxt är tom så ska man inte plussa utan bara sätta calculation till botValue och ändra topValueTxt.
        if (this.topValueTxt.getText().trim().isEmpty())
        {
            if (tmp == 0)
            {
                this.topValueTxt.setText("reciproc(" + this.botValue.toString() + ")");
                this.botValueTxt.setText("Det går inte att dela med noll.");
                this.freeze = true;

                return;
            }
            else
            {
                this.calculation = 1 / tmp;
                this.topValueTxt.setText("reciproc(" + this.botValue.toString() + ")");
                this.botValue.delete(0, this.botValue.length());
                this.botValue.append(this.calculation);
                this.botValueTxt.setText(this.botValue.toString());
            }
        }
        else
        {

            if (tmp != 0)
            {
                // Om topValueTxt inte är tomt
                if (this.latestOperator == Symbols.Add)
                    this.calculation += 1 / Double.parseDouble(this.botValue.toString());
                else if (this.latestOperator == Symbols.Sub)
                    this.calculation -= 1 / Double.parseDouble(this.botValue.toString());
                else if (this.latestOperator == Symbols.Multiply)
                    this.calculation *= 1 / Double.parseDouble(this.botValue.toString());
                else if (this.latestOperator == Symbols.Divide)
                    this.calculation /= 1 / Double.parseDouble(this.botValue.toString());
            }

            if (this.topValueTxt.getText().substring(this.topValueTxt.getText().length() - 1).equalsIgnoreCase(")"))
            {
                int t = this.topValueTxt.getText().lastIndexOf(" ");

                if (t == -1)
                    this.topValueTxt.setText("reciproc(" + this.topValueTxt.getText() + ")");
                else
                    this.topValueTxt.setText(this.topValueTxt.getText().substring(0, t + 1) + "reciproc(" + this.topValueTxt.getText().substring(t + 1) + ")");
            }
            else
                this.topValueTxt.setText(this.topValueTxt.getText() + "reciproc(" + this.botValue.toString() + ")");

            if (tmp == 0)
            {
                this.botValueTxt.setText("Det går inte att dela med noll.");
                this.freeze = true;

                return;
            }
            else
            {
                this.botValue.delete(0, this.botValue.length());
                this.botValue.append(tmp);
                this.botValueTxt.setText(this.botValue.toString());
            }
        }

        this.latestButton = Symbols.OneDividedByX;
    }//GEN-LAST:event_oneDividedByXBtnActionPerformed

    /**
     * Metod som körs varje gång användaren trycker på procent tecknet.
     * Metoden räknar ut procenten.
     * @param evt ActionEventet som skickas med.
     */
    private void percentBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_percentBtnActionPerformed
        // När man trycker på en operator så ska nästa tal man skriver in börja på nytt.
        this.newNumber = true;
        // Vi har inte skrivit ett kommatecken längre.
        this.comma = false;

        // Om topValueTxt är tom så ska man inte plussa utan bara sätta calculation till botValue och ändra topValueTxt.
        if (this.topValueTxt.getText().trim().isEmpty() || this.topValueTxt.getText().equalsIgnoreCase("0.0"))
        {
            this.calculation = 0.0;
            this.botValue.delete(0, this.botValue.length());
            this.botValue.append(this.calculation);
            this.botValueTxt.setText(this.botValue.toString());
            this.topValueTxt.setText(this.botValue.toString());
        }
        else
        {
            // Om topValueTxt inte är tomt
            double tmp = this.calculation * Double.parseDouble(this.botValue.toString()) / 100;

            if (this.latestOperator == Symbols.Add)
                this.calculation = this.calculation + (this.calculation * Double.parseDouble(this.botValue.toString()) / 100);
            else if (this.latestOperator == Symbols.Sub)
                this.calculation = this.calculation - (this.calculation * Double.parseDouble(this.botValue.toString()) / 100);
            else if (this.latestOperator == Symbols.Multiply)
                this.calculation = this.calculation * (this.calculation * (Double.parseDouble(this.botValue.toString()) / 100));
            else if (this.latestOperator == Symbols.Divide)
                this.calculation = this.calculation / (this.calculation * Double.parseDouble(this.botValue.toString()) / 100);

            this.botValue.delete(0, this.botValue.length());
            this.botValue.append(tmp);
            this.botValueTxt.setText(this.botValue.toString());
            this.topValueTxt.setText(this.topValueTxt.getText() + this.botValue.toString());
        }

        this.latestButton = Symbols.Percentage;
    }//GEN-LAST:event_percentBtnActionPerformed

    /**
     * Metod som körs varje gång användaren trycker på rooten ur tecknet.
     * Metoden räknar ut rooten ur.
     * @param evt ActionEventet som skickas med.
     */
    private void rootOfBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rootOfBtnActionPerformed
        // När man trycker på en operator så ska nästa tal man skriver in börja på nytt.
        this.newNumber = true;
        // Vi har inte skrivit ett kommatecken längre.
        this.comma = false;

        // Om topValueTxt är tom så ska man inte plussa utan bara sätta calculation till botValue och ändra topValueTxt.
        if (this.topValueTxt.getText().trim().isEmpty())
        {
            this.calculation = Math.sqrt(Double.parseDouble(this.botValue.toString()));
            this.topValueTxt.setText("sqrt(" + this.botValue.toString() + ")");
            this.botValue.delete(0, this.botValue.length());
            this.botValue.append(this.calculation);
            this.botValueTxt.setText(this.botValue.toString());
        }
        else
        {
            // Om topValueTxt inte är tomt
            if (this.latestOperator == Symbols.Add)
                this.calculation += Math.sqrt(Double.parseDouble(this.botValue.toString()));
            else if (this.latestOperator == Symbols.Sub)
                this.calculation -= Math.sqrt(Double.parseDouble(this.botValue.toString()));
            else if (this.latestOperator == Symbols.Multiply)
                this.calculation *= Math.sqrt(Double.parseDouble(this.botValue.toString()));
            else if (this.latestOperator == Symbols.Divide)
                this.calculation /= Math.sqrt(Double.parseDouble(this.botValue.toString()));

            if (this.topValueTxt.getText().substring(this.topValueTxt.getText().length() - 1).equalsIgnoreCase(")"))
            {
                int t = this.topValueTxt.getText().lastIndexOf(" ");

                if (t == -1)
                    this.topValueTxt.setText("sqrt(" + this.topValueTxt.getText() + ")");
                else
                    this.topValueTxt.setText(this.topValueTxt.getText().substring(0, t + 1) + "sqrt(" + this.topValueTxt.getText().substring(t + 1) + ")");
            }
            else
                this.topValueTxt.setText(this.topValueTxt.getText() + "sqrt(" + this.botValue.toString() + ")");

            double tmp = Math.sqrt(Double.parseDouble(this.botValue.toString()));

            this.botValue.delete(0, this.botValue.length());
            this.botValue.append(tmp);
            this.botValueTxt.setText(this.botValue.toString());
        }

        this.latestButton = Symbols.SquareRoot;
    }//GEN-LAST:event_rootOfBtnActionPerformed

    /**
     * Metod som körs varje gång användaren trycker på MC.
     * Metoden tar bort det talet som är sparat i minnet.
     * @param evt ActionEventet som skickas med.
     */
    private void memoryClearBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_memoryClearBtnActionPerformed
        if (this.memory != 0)
        {
            this.memory = 0;
            this.memoryTxt.setText("");
        }
    }//GEN-LAST:event_memoryClearBtnActionPerformed

    /**
     * Metod som körs varje gång användaren trycker på MR.
     * Metoden tar fram talet man sparade i minnet.
     * @param evt ActionEventet som skickas med.
     */
    private void memoryResumeBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_memoryResumeBtnActionPerformed
        if (this.memory != 0)
        {
            this.botValue.delete(0, this.botValue.length());
            this.botValue.append(this.memory);
            this.botValueTxt.setText(this.botValue.toString());
            this.latestButton = Symbols.Digit;
        }
        else
        {
            this.botValue.delete(0, this.botValue.length());
            this.botValue.append("0.0");
            this.botValueTxt.setText(this.botValue.toString());
        }
    }//GEN-LAST:event_memoryResumeBtnActionPerformed

    /**
     * Metod som körs varje gång användaren trycker på MS.
     * Metoden sparar det talet man har skrivit in i minnet.
     * @param evt ActionEventet som skickas med.
     */
    private void memorySaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_memorySaveActionPerformed
        double tmp = Double.parseDouble(this.botValue.toString());

        if (tmp != 0)
        {
            this.memory = tmp;
            this.memoryTxt.setText("M");
        }
    }//GEN-LAST:event_memorySaveActionPerformed

    /**
     * Metod som körs varje gång användaren trycker på M+.
     * Metoden tar talet man har i minne plus det talet man precis skrivit och
     * sparar detta i minnet.
     * @param evt ActionEventet som skickas med.
     */
    private void memoryPlusBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_memoryPlusBtnActionPerformed
        if (this.memory != 0)
        {
            this.memory += Double.parseDouble(this.botValue.toString());

            if (this.memory == 0)
                this.memoryTxt.setText("");
        }
    }//GEN-LAST:event_memoryPlusBtnActionPerformed

    /**
     * Metod som körs varje gång användaren trycker på M-.
     * Metoden tar talet man har i minne minus det talet man precis skrivit och
     * sparar detta i minnet.
     * @param evt ActionEventet som skickas med.
     */
    private void memoryMinusBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_memoryMinusBtnActionPerformed
        if (this.memory != 0)
        {
            this.memory -= Double.parseDouble(this.botValue.toString());

            if (this.memory == 0)
                this.memoryTxt.setText("");
        }
    }//GEN-LAST:event_memoryMinusBtnActionPerformed

    /**
     * Metod som körs när man trycker på "Currency" knappen.
     * Metoden konverterar från en valuta till en annan.
     * @param evt Action eventet som skickas med.
     */
    private void convertBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_convertBtnActionPerformed

        double from = this.currency.get(this.currencyFrom.getSelectedItem().toString());
        double to = this.currency.get(this.currencyTo.getSelectedItem().toString());

        double cur = 1 / from * to * Double.parseDouble(this.botValue.toString());

        this.botValue.delete(0, this.botValue.length());
        this.botValue.append(cur);
        this.botValueTxt.setText(this.botValue.toString());
        this.newNumber = true;

        this.fireConversionEvent(cur);
    }//GEN-LAST:event_convertBtnActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addBtn;
    private javax.swing.JButton backBtn;
    private javax.swing.JTextField botValueTxt;
    private javax.swing.JButton clearBtn;
    private javax.swing.JButton clearEntryBtn;
    private javax.swing.JButton commaBtn;
    private javax.swing.JButton convertBtn;
    private javax.swing.JComboBox currencyFrom;
    private javax.swing.JComboBox currencyTo;
    private javax.swing.JButton divideBtn;
    private javax.swing.JButton eightBtn;
    private javax.swing.JButton equalBtn;
    private javax.swing.JButton fiveBtn;
    private javax.swing.JButton fourBtn;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton memoryClearBtn;
    private javax.swing.JButton memoryMinusBtn;
    private javax.swing.JButton memoryPlusBtn;
    private javax.swing.JButton memoryResumeBtn;
    private javax.swing.JButton memorySave;
    private javax.swing.JTextField memoryTxt;
    private javax.swing.JButton multiplyBtn;
    private javax.swing.JButton nineBtn;
    private javax.swing.JButton oneBtn;
    private javax.swing.JButton oneDividedByXBtn;
    private javax.swing.JButton percentBtn;
    private javax.swing.JButton plusMinusBtn;
    private javax.swing.JButton rootOfBtn;
    private javax.swing.JButton sevenBtn;
    private javax.swing.JButton sixBtn;
    private javax.swing.JButton subBtn;
    private javax.swing.JButton threeBtn;
    private javax.swing.JTextField topValueTxt;
    private javax.swing.JButton twoBtn;
    private javax.swing.JButton zeroBtn;
    // End of variables declaration//GEN-END:variables

}