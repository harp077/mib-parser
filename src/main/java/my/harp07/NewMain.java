
package my.harp07;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.util.stream.Collectors.toList;
import net.percederberg.mibble.Mib;
import net.percederberg.mibble.MibLoader;
import net.percederberg.mibble.MibLoaderException;
import net.percederberg.mibble.MibSymbol;
import net.percederberg.mibble.MibValue;
import net.percederberg.mibble.MibValueSymbol;
import net.percederberg.mibble.value.ObjectIdentifierValue;
import org.apache.commons.lang3.StringUtils;

public class NewMain {
    
    private static String[] setKeys;
    private static ObjectIdentifierValue[] listValues;
    private static HashMap<String, ObjectIdentifierValue> hm = new HashMap<>();
    private static ObjectIdentifierValue bufOIV;
    private static String bufKEY;
    private static int curIndex=11;

    public static void main(String[] args) {
        try {
            // TODO code application logic here
            Mib mb = loadMib(new File("/usr/share/mibs/ietf/RFC1213-MIB"));
            System.out.println("well load");
            hm=extractOids(mb);
            setKeys=hm.keySet().toArray(new String[hm.size()]);
            listValues=hm.values().toArray(new ObjectIdentifierValue[hm.size()]);
            bufOIV=listValues[curIndex];
            bufKEY=setKeys[curIndex];
            System.out.println("key="+bufKEY);
            System.out.println("OIV.getName()="+bufOIV.getName());
            System.out.println("OIV.toAsn1String()="+bufOIV.toAsn1String());
            System.out.println("OIV.toDetailString()="+bufOIV.toDetailString());
            System.out.println("OIV.toString()="+bufOIV.toString()); 
            System.out.println("OIV.getSymbol()="+bufOIV.getSymbol());
            System.out.println("OIV.getSymbol().getText()="+bufOIV.getSymbol().getText());
            System.out.println("syntax="+StringUtils.substringBetween(bufOIV.getSymbol().getText(), "SYNTAX ", "ACCESS"));
            System.out.println("access="+StringUtils.substringBetween(bufOIV.getSymbol().getText(), "ACCESS", "STATUS"));
            System.out.println("info="+StringUtils.substringBetween(bufOIV.getSymbol().getText(), "DESCRIPTION", "::="));
        } catch (MibLoaderException ex) {
            Logger.getLogger(NewMain.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(NewMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static Mib loadMib(File file) throws FileNotFoundException, MibLoaderException, IOException {
        // In real code, a single MibLoader instance should be reused
        MibLoader loader = new MibLoader();
        // The MIB file may import other MIBs (often in same dir)
        loader.addDir(file.getParentFile());
        // Once initialized, MIB loading is straight-forward
        return loader.load(file);
    }

    public static ObjectIdentifierValue extractOid(MibSymbol symbol) {
        if (symbol instanceof MibValueSymbol) {
            MibValue value = ((MibValueSymbol) symbol).getValue();
            if (value instanceof ObjectIdentifierValue) {
                return (ObjectIdentifierValue) value;
            }
        }
        return null;
    }

    public static HashMap<String, ObjectIdentifierValue> extractOids(Mib mib) {
        HashMap<String, ObjectIdentifierValue> map = new HashMap<>();
        for (MibSymbol symbol : mib.getAllSymbols()) {
            ObjectIdentifierValue oid = extractOid(symbol);
            if (oid != null) {
                map.put(symbol.getName(), oid);
            }
        }
        return map;
    }

    public MibValueSymbol locateSymbolByOid(MibLoader loader, String oid) {
        ObjectIdentifierValue iso = loader.getRootOid();
        ObjectIdentifierValue match = iso.find(oid);
        return (match == null) ? null : match.getSymbol();
    }

    public void walkOidTree(Mib mib) {
        MibValueSymbol symbol = mib.getRootSymbol();
        MibValue value = (symbol == null) ? null : symbol.getValue();
        if (value instanceof ObjectIdentifierValue) {
            walkOidTree(mib, (ObjectIdentifierValue) value);
        }
    }

    private void walkOidTree(Mib mib, ObjectIdentifierValue oid) {
        MibValueSymbol symbol = oid.getSymbol();
        if (symbol != null && symbol.getMib() != mib) {
            return; // External MIB attached here
        }
        // <-- Process OID here
        for (int i = 0; i < oid.getChildCount(); i++) {
            walkOidTree(mib, oid.getChild(i));
        }
    }

}
