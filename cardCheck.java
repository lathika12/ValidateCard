import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import static java.lang.Thread.sleep;

/**
 * Created by Usewell on 1/29/2017.
 */
public class cardCheck {

    public static int maxentry = 0;
    public static String cardNumber = null;
    public static Long cardNumeral = null;
    public static ArrayList<String> cardDetails = new ArrayList<String>();

    public static void main(String[] args) throws Exception{

        cardNumeral = enterCardNumber(maxentry);

        // Check the .csv file to find if the card is already saved else insert into the file
        boolean dataval = checkCardHistory();

        if(dataval == true){
            //Checking the length for card type
            findCardType(cardNumber.length());
            //Checking for MII - Major Industry Identifier
            String mii = findMII();
            //Checking for BIN - Bank Identification Number
            String bin = findBIN();
            //Finding user account number
            String accnum = findUserAcctNum();
            //Validate the validity of card using checksum implementing mod 10 algorithm - implementing check digit test
            String validity = checkCardValidity();
            cardDetails.clear();
            cardDetails.add(cardNumber);
            cardDetails.add(mii);
            cardDetails.add(bin);
            cardDetails.add(accnum);
            cardDetails.add(validity);
            // write the data to csv filee
            writeDataToCSV(cardDetails);
            System.out.println("The system found the following information:");
            System.out.println(" Industry: "+ cardDetails.get(1) +"\n Issuer/brand: " + cardDetails.get(2) +
                    "\n user account: " + cardDetails.get(3) + " \n card validity : " + cardDetails.get(4) );

        }else{
            // Card details already exists display them.
            System.out.println("The system found the following information:");
            System.out.println(" Industry: "+ cardDetails.get(1) +"\n Issuer/brand: " + cardDetails.get(2) +
                    "\n user account: " + cardDetails.get(3) + " \n card validity : " + cardDetails.get(4) );
        }
    }

    public static void writeDataToCSV(ArrayList<String> carddetstoinsert) throws IOException {

        StringBuffer sb = new StringBuffer();
        sb.append(cardDetails.get(0).toString());
        sb.append(",");
        sb.append(cardDetails.get(1).toString());
        sb.append(",");
        sb.append(cardDetails.get(2).toString());
        sb.append(",");
        sb.append(cardDetails.get(3).toString());
        sb.append(",");
        sb.append(cardDetails.get(4).toString());
        String content = sb.toString();

        BufferedWriter out = new BufferedWriter(new FileWriter("cardInfo.csv",true));
        out.write("\n"+content);
        out.close();
        BufferedReader in = new BufferedReader(new FileReader("cardInfo.csv"));
        String str;
        while ((str = in.readLine()) != null) {
        }
        in.close();
    }

    public static boolean checkCardHistory(){

        File new_acq = new File("cardInfo.csv");
        Scanner acq_scan = null;
        boolean flag = true;

        try {
            acq_scan = new Scanner(new_acq);
        } catch (FileNotFoundException ex) {
            System.out.println("File not found. Please check the path." + ex);
        }
        String cardnumber_csv;
        String industry;
        String brand;
        String accountnumber;
        String validity;

        while (acq_scan.hasNext()) {
            String line = acq_scan.nextLine();
            if (line.charAt(0) == '#') {
                continue;
            }
            String[] split = line.split(",");
            cardnumber_csv = split[0];
            industry = split[1];
            brand = split[2];
            accountnumber = split[3];
            validity = split[4];

            if (cardnumber_csv.equals(cardNumber)){
                flag = false;
                cardDetails.add(cardnumber_csv);
                cardDetails.add(industry);
                cardDetails.add(brand);
                cardDetails.add(accountnumber);
                cardDetails.add(validity);
                break;
            }

        }
        acq_scan.close();

        if(flag == false)
            return flag;
        else
            return flag;

    }

    public static String checkCardValidity(){

        //Luhn formula or mod 10 check
        // 1. reverse the card id number
        StringBuffer cardnumrev = new StringBuffer(cardNumber);
        char charreversed[] = cardnumrev.reverse().toString().toCharArray();
        // 2. Multiply the first digit with 1, the second with 2, the third with 1, and keep alternating -
        //i. If the result of multiplying the number with 2 was >= 10, add the 2 digits of the
        //   result to get a single digit (e.g., 2 x 9 = 18, so 1 + 8 = 9)
        int mulresults[] = new int[cardNumber.length()];
        for (int iteratorvar = cardNumber.length() , i=2 ; iteratorvar > 0 ; iteratorvar-- , i++ ){
            if(i%2 == 1){
                int resval = (Character.getNumericValue(charreversed[i-2]))*2;
                if(resval / 10 == 0){
                    mulresults[i-2] = resval;
                }else {
                    int temp = resval % 10 ;
                    temp = temp + 1;
                    mulresults[i-2] = temp;
                }
            }else{
                mulresults[i-2] = (Character.getNumericValue(charreversed[i-2]));
            }
        }
        // 3. Add all the digits resulted from the multiplication.
        // i. If the answer mod 10 equals zero, then the card number is valid. Other than
        // that, the number is not valid.
        int k = 0 , mulressum = 0;
        do{
            mulressum +=  (int) mulresults[k];
        }while ( k++ < cardNumber.length()-1 );
        if(mulressum % 10 ==0 ){
            return "Valid";
        }else {
            return "Invalid";
        }
    }

    public static String findUserAcctNum(){
        char numchararray[] = cardNumeral.toString().toCharArray();
        StringBuffer acctnumber = new StringBuffer();
        int j = 6;
        while(j < cardNumber.length()-1){
            acctnumber = acctnumber.append(numchararray[j]);
            j++;
        }
        return acctnumber.toString();
    }

    public static String findBIN(){
        String binVar = null;
        int rem = (int) (cardNumeral/Math.pow(10,cardNumber.length()-6));
        if( (int) (rem/Math.pow(10,4)) == 34 || (int) (rem/Math.pow(10,4)) == 37 ){
            binVar = "Amex";
        }else if((int) (rem/Math.pow(10,5)) == 4){
            binVar = "Visa";
        }else if( (int) (rem/Math.pow(10,4)) == 51 || (int) (rem/Math.pow(10,4)) == 55 ){
            binVar = "MasterCard";
        }else if( (int) (rem/Math.pow(10,2)) == 6011 || (int) (rem/Math.pow(10,3)) == 644 || (int) (rem/Math.pow(10,2)) == 65){
            binVar = "Discover";
        }else{
            binVar = "Unrecognized";
        }
        return binVar;
    }

    public static String findMII(){
        String MII[] = {"Airlines","Airlines","Travel","Banking and Financial","Banking and Financial","Merchandising and Banking/Financial","Petroleum","Healthcare, Telecommunications","National Assignment"};
        int rem = (int) (cardNumeral/Math.pow(10,cardNumber.length()-1));
        return MII[rem];
    }

    public static void findCardType(int length){
        HashMap<Integer,ArrayList<String>> typeOfCard = new HashMap<Integer, ArrayList<String>>();
        ArrayList<String> types = new ArrayList<String>();
        types.add("Visa");
        typeOfCard.put(13,types);
        types.clear();
        types.add("DinersClub");
        typeOfCard.put(14,types);
        types.clear();
        types.add("American Express");
        types.add("Diners Club");
        typeOfCard.put(15,types);
        types.clear();
        types.add("Visa");
        types.add("Diners Club");
        types.add("MasterCard");
        types.add("Discover");
        typeOfCard.put(16,types);
    }

    public static Long enterCardNumber(int maxentry) throws InterruptedException {
        int countdown=30;
        Long cardNumeral=null;
        Scanner scanner = new Scanner(System.in);
        if(maxentry < 3){
            System.out.print("Enter a valid Card Number:");
            try{
                cardNumber = scanner.next();
                if(cardNumber.length() < 13 || cardNumber.length() > 16){
                    maxentry++;
                    System.out.println("Entered card number is invalid...");
                    enterCardNumber(maxentry);
                }
                cardNumeral = Long.parseLong(cardNumber);
            }catch(Exception e){
                maxentry++;
                System.out.println("Entered card number is invalid...");
                enterCardNumber(maxentry);
                }
            }
            else {
            System.out.println("You've entered an invalid card number. Please try again after 30 seconds.");
            while(countdown > 0){
                System.out.println("..."+countdown);
                sleep(1000);
                countdown--;
            }
            maxentry=0;
            enterCardNumber(maxentry);
        }
        return cardNumeral;
    }
}
