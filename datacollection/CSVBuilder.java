package datacollection;

/**
 * @author Benjamin Albert
 */
public class CSVBuilder {
    private StringBuilder stringBuilder;
    private boolean firstColumn;
    
    public CSVBuilder(){
        stringBuilder = new StringBuilder();
        firstColumn = true;
    }
    
    public void conditionalAppendComma(){
        if (!firstColumn){
            stringBuilder.append(",");
        }
        firstColumn = false;
    }
    
    public CSVBuilder append(CharSequence charSequence){
        conditionalAppendComma();
        stringBuilder.append(charSequence);
        return this;
    }
    
    public CSVBuilder append(int i){
        conditionalAppendComma();
        stringBuilder.append(i);
        return this;
    }
    
    public CSVBuilder append(double d){
        conditionalAppendComma();
        stringBuilder.append(d);
        return this;
    }
    
    public void newline(){
        stringBuilder.append("\n");
        firstColumn = true;
    }
    
    public String toCSV(){
        return stringBuilder.toString();
    }
}