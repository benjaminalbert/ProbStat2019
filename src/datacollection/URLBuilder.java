package datacollection;

/**
 * @author Benjamin Albert
 */
public class URLBuilder {
    private StringBuilder stringBuilder;
    private boolean firstColumn;
    
    public URLBuilder(String baseURL){
        stringBuilder = new StringBuilder();
        stringBuilder.append(baseURL);
        firstColumn = true;
    }
    
    public URLBuilder addArg(String argName, String argValue){
        stringBuilder.append(firstColumn ? "?" : "&");
        stringBuilder.append(argName);
        stringBuilder.append("=");
        stringBuilder.append(argValue);
        firstColumn = false;
        return this;
    }
    
    public String toURL(){
        return stringBuilder.toString();
    }
}
