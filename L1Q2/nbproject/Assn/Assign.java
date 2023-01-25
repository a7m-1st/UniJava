package Assn;
//note: add number of jobs killed & completed
//average cpus used

/* to Add:
-give months, gives average jobs

Progress:
-Two main hashmaps present
--mapOfList contains all the lines numbered sequentially
--LineToIdMap 1 & 2 contains the Line number to JobId (JobId as key)
*/
import com.sun.security.jgss.GSSUtil;
import org.w3c.dom.ls.LSOutput;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import Swift.*;
import Assn.*;

import javax.swing.*;


public class Assign {
    //String jobId[] = new String[10];
    static HashMap<Integer, String> mapOfList = new HashMap<>(); //map JobId to lineNumber
    //Line to id map
    static HashMap<String, Integer> lineToIdMap = new HashMap<>(); //first occurrence
    static HashMap<String, Integer> lineToIdMap2 = new HashMap<>(); //last occurrence
    static ArrayList<Double> avgTimes = new ArrayList<>(); //contains the time excecution
    static int lineNum=0; static boolean gate = true;
    static JFrame frame; static graph fun;

    public static void main(String[] args) {
        fun = new graph();
        frame = new JFrame();
        Scanner in = new Scanner(System.in); Scanner in2 = new Scanner(System.in);
        String filename = "C:\\Users\\A7M1ST\\Downloads\\extracted_log.txt";
        String line, jobId;

        System.out.print("Please choose whether you want to display the errors- 1 for yes");
        boolean choose = in.nextInt() == 1;

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

                if(choose) numberOfErrors(line); //ERRORS
            }

            file.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        if(choose) System.out.println("Total jobs with errors: " + count + "\n");
        panelCreator2.scale = 15000;
        System.out.println("Scale of graph set to " + panelCreator2.scale); //set scale to line Numbers


        int c = 0;
        //replace with -49 the starting errors
        for(var x: lineToIdMap.entrySet()) {
            String lineCheck = mapOfList.get(x.getValue()); //links value to line efficiently
            String[] arr2 = lineCheck.split(" "); //split the array
            if(!(Objects.equals(arr2[1],"_slurm_rpc_submit_batch_job:") || Objects.equals(arr2[1],"sched:"))) {
                lineToIdMap.put(x.getKey(), -49); //removes the line incomplete start data
                //System.out.println(x.getKey());//debug
                c++; //counter of end only lines
            }
        }
        System.out.println("Num of Jobs which ended but started before timeline: (including errors) " + panelCreator2.clr[panelCreator2.i]+ c);
        fun.createPanel(c, frame);
        //System.out.println(lineToIdMap);

        int c2 = 0, kill = 0, complete =0;
        //replace with -49 the *ending* errors
        for(var x: lineToIdMap2.entrySet()) {
            String lineCheck = mapOfList.get(x.getValue()); //links value to line efficiently
            String[] arr2 = lineCheck.split(" "); //split the array
            if((Objects.equals(arr2[1],"_slurm_rpc_submit_batch_job:") || Objects.equals(arr2[1],"sched:"))) {
                //System.out.println(mapOfList.get(x.getValue()));//debug
                lineToIdMap2.put(x.getKey(), -49); //replace the line number to -49 so cannot be found
                c2++; //counter of end only lines
            }
            if(Objects.equals(arr2[1],"_slurm_rpc_kill_job:")) kill++;
            if(Objects.equals(arr2[1],"_job_complete:")) complete++;
        }
        System.out.println("Num of Jobs which started but ended after timeline: (excluding errors) in " + panelCreator2.clr[panelCreator2.i]+ c2);
        fun.createPanel(c2, frame);

        System.out.println("Num of Jobs completed succesfully: in " + panelCreator2.clr[panelCreator2.i] + complete);
        fun.createPanel(complete, frame);

        System.out.println("Num of Jobs killed in " + panelCreator2.clr[panelCreator2.i] + kill);
        fun.createPanel(kill, frame);

        System.out.println("There is " + lineToIdMap.size() + " jobIds");
        System.out.println("There is " + lineNum + " lines");

        //fill up the times array with time for excecution
        if(gate) for(var x: lineToIdMap.keySet()) {
            idStartEnd(x);
        }
        gate = false;
        System.out.printf("Average excecution time is %.2f seconds, %.2f minutes, %.2f hours in %s\n", avg(), avg()/60, avg()/3600, panelCreator2.clr[panelCreator2.i]);
        fun.createPanel((int) avg()/3600, frame);
        System.out.printf("Max time is %.2f seconds = %.2f days\n", Collections.max(avgTimes), Collections.max(avgTimes)/3600/24);
        System.out.printf("Min time is %.2f seconds \n", Collections.min(avgTimes));

        System.out.println("*************");
        searchInfo("Partition=cpu", "-epyc", "-opteron"); //search partition info
        System.out.println();
        searchInfo("Partition=gpu","-v100s", "-k40c", "-titan", "-k10"); //search partition info

        System.out.println("Fetching Complete!\n");

        //enter jobid to get searched
        while(true) {
            System.out.print("Please enter jobId to get searched; -1 to continue to time segments");
            int searched = in.nextInt();
            if(searched == -1) break;
            idStartEnd(Integer.toString(searched));
        }

        //enter times to get searched
        while(true) {
            System.out.print("Please enter start time; -1 to quit:");
            String searched1 = in2.nextLine();
            if(Objects.equals(searched1, "-1")) break;
            System.out.print("Please enter end time:");
            String searched2 = in2.nextLine();
            getTime(searched1, searched2); //times
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

        if (firstLine != null && lastLine != null) { //as long as "null" is not recieved
            //Finding time:::
            //time1 =  [2022-06-01T01:02:35.148]
            //time2 =  [2022-06-01T09:11:17.689]
            String[] firstSplit = firstLine.split(" ");
            String time1 = firstSplit[0];

            String[] lastSplit = lastLine.split(" ");
            String time2 = lastSplit[0];
            if(!gate) {
                System.out.println("*************");
                System.out.println("Info of " + id);
                System.out.printf("Start of is %s \nEnd is %s \n", firstOcc, lastOcc);
                System.out.println("Number of lines interval: " + (lastOcc - firstOcc));

                System.out.println("\nStart: " + firstLine);
                System.out.println("End: " + lastLine);
                System.out.println();
            }

            getTime(time1, time2);
        } else if(!gate) System.out.println("JobId not found, Please try again");
    }

    //Searches then counts what ever value inputted
    //output= //gets PARTITIONS
    //Takes maximum 5 values to be searched within search
    public static void searchInfo(String searched, String ... v) {
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
            System.out.println("Number of " + v[0] + " users is (in " + panelCreator2.clr[panelCreator2.i] + ") " + a);
            fun.createPanel(a, frame);
            System.out.println("Number of " + v[1] + " users is (in " + panelCreator2.clr[panelCreator2.i] + ") " + b);
            fun.createPanel(b, frame);
            System.out.println("Number of " + v[2] + " users is (in " + panelCreator2.clr[panelCreator2.i] + ") " + c);
            fun.createPanel(c, frame);
            System.out.println("Number of " + v[3] + " users is (in " + panelCreator2.clr[panelCreator2.i] + ") " + d);
            fun.createPanel(d, frame);
            System.out.println("Number of " + v[4] + " users is (in " + panelCreator2.clr[panelCreator2.i] + ") " + e);
            fun.createPanel(e, frame);
        } catch(ArrayIndexOutOfBoundsException ignored) {}

    }

    //Gets time diff AND number of jobs submitted & ended between two times
    //return long in order to calculate average time taken
    public static void getTime(String timeA, String timeB) {
        //timeA =  [2022-06-01T01:02:35.148]
        //timeB =  [2022-06-01T09:11:17.689]

        //timeA get date & time
        try {
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
            double diff = d2.getTime() - d1.getTime();
            double diffSeconds = diff / 1000;
            double diffMinutes = diff / (60 * 1000);
            double diffHours = diff / (60 * 60 * 1000);

            if(!gate) {
                System.out.printf("Time in seconds: %.2f seconds.\n", diffSeconds);
                System.out.printf("Time in minutes: %.2f minutes.\n", diffMinutes);
                System.out.printf("Time in hours: %.2f hours.\n", diffHours);


                //Calculate the in between
                int firstLineNum = 0, lastLineNum = 0;
                //find lineNum of timeA & timeB
                for (int i = 1; i <= lineNum; i++) {
                    String[] arr = mapOfList.get(i).split(" "); //contains the date as arr[0]
                    if (timeA.equals(arr[0])) firstLineNum = i;
                    if (timeB.equals(arr[0])) { //if first & last line is found; break
                        lastLineNum = i;
                        break;
                    }
                }

                int jobsCounter = 0, jobsCounterEnd = 0;
                //jobs started between the line numbers
                for (int i = firstLineNum; i < lastLineNum; i++) //go through each line number
                    if (lineToIdMap.containsValue(i)) {
                        //System.out.println(mapOfList.get(i)); //debug
                        jobsCounter++;
                    }


                System.out.println("Jobs submitted between the time is: " + jobsCounter);

                for (int i = firstLineNum; i < lastLineNum; i++)
                    if (lineToIdMap2.containsValue(i)) {
                        //System.out.println(mapOfList.get(i)); //debug
                        jobsCounterEnd++;
                    }
                System.out.println("Jobs ended between the time is: " + jobsCounterEnd);
                System.out.println("*************");
            }


            avgTimes.add(diffSeconds);
        } catch(StringIndexOutOfBoundsException e) {
            System.out.println("Please Enter Correct Time");
        }
    }

    static int count = 0;
    public static void numberOfErrors(String line) {
        String[] arr = line.split(" ");
        if(arr[1].equals("error:")) {
            if(!getSearched(line, "user=").equals("NAN"))
             System.out.println("Error with username " + getSearched(line, "user=") + " at line " + lineNum);
            count++;
        }

    }

    public static float avg() {
        float S = 0;
        for(var x: avgTimes) S += x;
        return S / avgTimes.size();
    }
}
