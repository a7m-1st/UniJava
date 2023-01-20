package Assn;
//note: add number of jobs killed & completed
//average cpus used
//average excecution time
//average number of jobs submitted & ended within time
//number of jobs causing errors & user

/*
Progress:
-Two main hashmaps present
--mapOfList contains all the lines numbered sequentially
--LineToIdMap 1 & 2 contains the Line number to JobId (JobId as key)
*/
import com.sun.security.jgss.GSSUtil;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.Scanner;



public class Assign {
    //String jobId[] = new String[10];
    static HashMap<Integer, String> mapOfList = new HashMap<>(); //map JobId to lineNumber
    //Line to id map
    static HashMap<String, Integer> lineToIdMap = new HashMap<>(); //first occurrence
    static HashMap<String, Integer> lineToIdMap2 = new HashMap<>(); //last occurrence
    static int lineNum=0;

    public static void main(String[] args) {
        String filename = "C:\\Users\\A7M1ST\\Downloads\\extracted_log.txt";
        String line, jobId;

        //initialize lines & jobid to hashmaps
        try {
            Scanner file = new Scanner(new FileInputStream(filename));

            while(file.hasNext()) {
                lineNum++; //Number of lines counter
                line = file.nextLine(); //Full Line
                mapOfList.put(lineNum, line); //Assign to a line number to a line

                jobId = getJobId(line); //jobID, NAN if not id
                if(!Objects.equals(jobId, "NAN")) {
                    lineToIdMap.putIfAbsent(jobId, lineNum); //First occurance of id
                    lineToIdMap2.put(jobId, lineNum);    //last occurance of id
                }
            }

            file.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }


        System.out.println("There is " + lineToIdMap.size() + " jobIds");
        System.out.println("There is " + lineNum + " lines");
        System.out.println("Fetching Complete!\n");

        idStartEnd("42834");

        searchInfo("Partition=cpu", "-epyc", "-opteron"); //search partition info
        searchInfo("Partition=gpu","-v100s", "-k40c", "-titan", "-k10"); //search partition info
        getTime("[2022-06-01T01:02:35.148]", "[2022-06-01T08:55:12.708]");

        for(var x: lineToIdMap.entrySet()) {


        }
    }


    //Get job Id; return "NAN" if not found
    //This stops before a space & end of line
    public static String getJobId(String line) {
        String id = "NAN";
        int i; char nextChar;
        int value = line.indexOf("JobId=");
        if(value > -1) {
            for(i=0; i<line.length(); i++) {
                try {
                    nextChar = line.charAt(value + i);
                } catch(StringIndexOutOfBoundsException e) {
                    break;
                }
                if(nextChar == ' ') {
                    //decrement i before exiting
                    break;
                }
            }

            id = line.substring(value+6, value+i);
        }

        return id;
    }

    //searches of whatever values; stops at ' ' & returns string
    public static String getSearched(String line, String searched) {
        String id = "NAN";
        int i; char nextChar;
        int value = line.indexOf(searched);

        if(value > -1) {
            for(i=0; i<line.length(); i++) {
                try {
                    nextChar = line.charAt(value + i);
                } catch(StringIndexOutOfBoundsException e) {
                    break;
                }
                if(nextChar == ' ') {
                    break;
                }
            }

            id = line.substring(value+searched.length(), value+i);
        }

        return id;
    }

    //GETS LINE & LINE NUMBER OF START & END
    //GETS TIMES OF START & END
    public static void idStartEnd(String id) {
        Integer firstOcc = lineToIdMap.get(id);
        Integer lastOcc = lineToIdMap2.get(id);

        String firstLine = mapOfList.get(firstOcc);
        String lastLine = mapOfList.get(lastOcc);

            System.out.println("*************");
        System.out.println("Info of " + id);
        System.out.printf("Start of is %s \nEnd is %s \n", firstOcc, lastOcc);
        System.out.println("Number of lines interval: " + (lastOcc - firstOcc));

        System.out.println("\nStart: " + firstLine);
        System.out.println("End: " + lastLine);


        //Finding time:::
        //time1 =  [2022-06-01T01:02:35.148]
        //time2 =  [2022-06-01T09:11:17.689]

        String[] firstSplit = firstLine.split(" ");
        String time1 = firstSplit[0];

        String[] lastSplit = lastLine.split(" ");
        String time2 = lastSplit[0];

        getTime(time1, time2);
    }

    //Searches then counts what ever value inputted
    //output= number of values
    //Takes maximum 5 values to be searched within search
    public static void searchInfo(String searched, String ... v) {
        System.out.println("*************");
        int counter = 0,a=0, b=0, c=0, d=0, e=0;
        String found;

        for(int i=1; i<lineNum; i++) {
            found = getSearched(mapOfList.get(i), searched); //get each line & search for the element
            if (!found.equals("NAN")) { //increment if found
                //if value equals
                try {
                    if (found.equals(v[0])) a++;
                    if (found.equals(v[1])) b++;
                    if (found.equals(v[2])) c++;
                    if (found.equals(v[3])) d++;
                    if (found.equals(v[4])) e++;
                } catch(ArrayIndexOutOfBoundsException ignored){};

                counter++;
            }
        }

        System.out.println("Number of " + searched + " is " + counter);
        try {
            System.out.println("Number of " + v[0] + " users is " + a);
            System.out.println("Number of " + v[1] + " users is " + b);
            System.out.println("Number of " + v[2] + " users is " + c);
            System.out.println("Number of " + v[3] + " users is " + d);
            System.out.println("Number of " + v[4] + " users is " + e);
        } catch(ArrayIndexOutOfBoundsException ignored) {}

    }

    public static void getTime(String timeA, String timeB) {
        //timeA =  [2022-06-01T01:02:35.148]
        //timeB =  [2022-06-01T09:11:17.689]

        //timeA get date & time
        String date = timeA.substring(1, 11);
        String time = timeA.substring(12, 24); //09:11:17
        String dateStart = date + " " + time; //convert to compatible format

        //timeB get date & time
        date = timeB.substring(1, 11);  //2022-06-01
        time = timeB.substring(12, 24); //09:11:17
        String dateStop = date + " " + time;

        // Custom date format
        SimpleDateFormat format = new SimpleDateFormat("yy-MM-dd HH:mm:ss");

        Date d1 = null;
        Date d2 = null;
        try {
            d1 = format.parse(dateStart);
            d2 = format.parse(dateStop);
        } catch (ParseException e) {
            e.printStackTrace();
        }

    // Get msec from each, and subtract.
        assert d2 != null;
        long diff = d2.getTime() - d1.getTime();
        long diffSeconds = diff / 1000;
        long diffMinutes = diff / (60 * 1000);
        long diffHours = diff / (60 * 60 * 1000);

        System.out.println("*************");
        System.out.println("Time in seconds: " + diffSeconds + " seconds.");
        System.out.println("Time in minutes: " + diffMinutes + " minutes.");
        System.out.println("Time in hours: " + diffHours + " hours.");


        //Calculate the in between
        int firstLineNum=0, lastLineNum=0;
        //find lineNum of timeA & timeB
        for(int i=1; i<=lineNum; i++) {
            String[] arr = mapOfList.get(i).split(" "); //contains the date as arr[0]
            if(timeA.equals(arr[0])) firstLineNum = i;
            if(timeB.equals(arr[0])) { //if first & last line is found; break
                lastLineNum = i;
                break;
            }
        }

        int jobsCounter=0;
        //jobs started between the line numbers
        for(int i=firstLineNum; i<lastLineNum; i++)
            if(lineToIdMap.containsValue(i)) {
                System.out.println(mapOfList.get(i));
                jobsCounter++;
            }


        System.out.println("Jobs submitted between line " + firstLineNum + " & " + lastLineNum + ": " + jobsCounter);

    }
}
