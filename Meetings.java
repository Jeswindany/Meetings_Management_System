import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Meetings {
    static Scanner sc = new Scanner(System.in);
    public static void main(String[] args) {
        List<List<String>> meetings = new ArrayList<>();
        int choice;

        do { 
            System.out.println("MENU\n");
            System.out.println("1. Add Schedule");
            System.out.println("2. View Schedule");
            System.out.println("3. Give Schedule");
            System.out.println("4. Exit");
            System.out.print("\nEnter your choice (1/2/3/4): ");
            choice = sc.nextInt();
            System.out.println();

            switch (choice) {
                case 1 -> {
                    System.out.print("Enter the start time (hh:mm in 24hrs format): ");
                    String start = sc.next();
                    System.out.print("Enter the end time (hh:mm in 24hrs format): ");
                    String end = sc.next();

                    // Input Validation
                    if (!isValid(start, end)) {
                        System.out.println("Invalid time! The time should be in the format (hh:mm)\n");
                        continue;
                    }

                    addMeeting(meetings, start, end);
                }
                case 2 -> {
                    System.out.println("Allocated meetings: ");
                    for (List<String> meeting: meetings) {
                        System.out.println(meeting);
                    }
                    System.out.println();
                    break;
                }
                case 3 -> {
                    System.out.print("Enter duration (in mins): ");
                    int duration = sc.nextInt();

                    // Input Validation
                    if (duration <= 0) {
                        System.out.println("Invalid duration");
                        continue;
                    }

                    giveSchedule(meetings, duration);
                }
                case 4 -> {
                    System.out.println("Exiting application...");
                    break;
                }
                default -> System.out.println("\nInvalid choice!\n");
            }
        } while (choice != 4);
        sc.close();
    }

    public static boolean isValid(String start, String end) {
        String[] s1 = start.split(":");
        String[] s2 = end.split(":");

        // Checking format
        if (s1.length != 2 || s2.length != 2) {
            return false;
        }

        // Checking for valid hours
        if (Integer.parseInt(s1[0]) >= 24 || Integer.parseInt(s1[0]) < 0 || Integer.parseInt(s2[0]) >= 24 || Integer.parseInt(s2[0]) < 0) {
            return false;
        }

        // Checking for valid minutes
        if (Integer.parseInt(s1[1]) >= 60 || Integer.parseInt(s1[1]) < 0 || Integer.parseInt(s2[1]) >= 60 || Integer.parseInt(s2[1]) < 0) {
            return false;
        }

        int[] startTime = convertToNumber(start);
        int[] endTime = convertToNumber(end);

        if (endTime[0] < startTime[0] ) {
            return false;
        } else if (endTime[0] == startTime[0] && endTime[1] < startTime[1]) {
            return false;
        }

        return true;
    }
    
    // Useful to compare times as Integers
    public static int[] convertToNumber(String time) {
        String[] arr = time.split(":");
        return new int[] {Integer.parseInt(arr[0]), Integer.parseInt(arr[1])};
    }

    public static void addMeeting(List<List<String>> meetings, String start, String end) {
        if (meetings.isEmpty()) {
            meetings.add(List.of(start, end));
            System.out.println("Schedule added succesfully\n");
            return;
        }

        // startHour, startMin -> Input start time
        int[] startTime = convertToNumber(start);
        int startHour = startTime[0];
        int startMin = startTime[1];
        
        // The meetings are inserted in ascending order
        // Therefore, the below code finds the position of the new meeting to be inserted,
        // by comparing the start time of the input time with the end time of the list meetings 
        int i = 0;
        while (i < meetings.size()) {
            String currMeet = meetings.get(i).get(1);
            int[] time = convertToNumber(currMeet);
            int currEndHour = time[0];
            int currEndMin = time[1];

            if (currEndHour < startHour) i++;
            else if (currEndHour == startHour && currEndMin <= startMin) i++;
            else break; 
        }

        // All the meetings are before the given start time:
        if (i == meetings.size()) {
            meetings.add(new ArrayList<>(List.of(start, end)));
            System.out.println("Schedule added succesfully\n");
            return;
        }

        // Inserting a meeting
        String currMeet = meetings.get(i).get(0);
        boolean overlap = isOverlapping(start, end, currMeet);

        if (!overlap) {
            meetings.add(new ArrayList<>(List.of(start, end)));
            System.out.println("Schedule added succesfully\n");
        } else {
            System.out.println("The given schedule cannot be added\n");

            // Converting the required duration to minutes
            int[] endTime = convertToNumber(end);
            int endHour = endTime[0];
            int endMin = endTime[1];

            // converting to duration in minutes
            int duration = calculateDuration(startHour, endHour, startMin, endMin);

            giveSchedule(meetings, duration);
        }
    }

    public static void giveSchedule(List<List<String>> meetings, int duration) {
        // Searching available time inbetween schedules
        for (int i = 0; i < meetings.size() - 1; i++) {

            // End time of the current meeting
            String endTime = meetings.get(i).get(1);

            // Start time of the next meeting
            String startTime = meetings.get(i + 1).get(0);

            int[] currEnd = convertToNumber(endTime);
            int[] nextStart = convertToNumber(startTime);

            int gap = calculateDuration(currEnd[0], nextStart[0], currEnd[1], nextStart[1]);
            if (gap >= duration) {
                String newEndTime = findEndTime(endTime, duration);
                System.out.println("The recommended schedule: " + endTime + ", " + newEndTime);
                System.out.print("Do you want to add this schedule? (y/n): ");
                char ch = sc.next().charAt(0);
                if (ch == 'y') {
                    meetings.add(i + 1, List.of(endTime, newEndTime));
                    System.out.println("Schedule added succesfully\n");
                }
                return;
            }
        }

        // If durations inbetween already saved meetings are not enough
        // Check if there is time available to right of the last meeting

        String lastMeet = meetings.get(meetings.size() - 1).get(1);
        int[] lastTime = convertToNumber(lastMeet);
        
        int gap = calculateDuration(lastTime[0], 24, lastTime[1], 0);
        if (gap >= duration) {
            String newEndTime = findEndTime(lastMeet, duration);
            System.out.println("The recommended schedule: " + lastMeet + ", " + newEndTime);
            System.out.print("Do you want to add this schedule? (y/n): ");
            char ch = sc.next().charAt(0);
            if (ch == 'y') {
                meetings.add(List.of(lastMeet, newEndTime));
                System.out.println("Schedule added succesfully\n");
            }
            return;
        }

        // Check if there is time available to right of the last meeting

        String firstMeet = meetings.get(0).get(0);
        int[] firstTime = convertToNumber(firstMeet);
        
        gap = calculateDuration(0, firstTime[0], 0, firstTime[1]);
        if (gap >= duration) {
            String newEndTime = findEndTime("00:00", duration);
            System.out.println("The recommended schedule: 00:00, " + newEndTime);
            System.out.print("Do you want to add this schedule? (y/n): ");
            char ch = sc.next().charAt(0);
            if (ch == 'y') {
                meetings.add(0, List.of("00:00", newEndTime));
                System.out.println("Schedule added succesfully\n");
            }
            return;
        }

        System.out.println("No time slots are available for the given duration.\n");
    } 

    public static boolean isOverlapping(String start, String end, String currStart) {
        // Return value:
        // false -> no overlap
        // true -> overlap

        // Input end time
        int[] endTime = convertToNumber(end);
        int endHour = endTime[0];
        int endMin = endTime[1];

        // Current list element start time
        int[] time = convertToNumber(currStart);
        int currStartHour = time[0];
        int currStartMin = time[1];

        if (endHour < currStartHour) {
            return false;
        } else if (endHour == currStartHour && endMin <= currStartMin) {
            return false;
        } else {
            return true;
        }
    }

    // calculates duration in minutes
    public static int calculateDuration(int startHour, int endHour, int startMin, int endMin) {
        // converting hours to minutes
        int duration = (endHour - startHour) * 60;

        // adding minutes
        if (startHour == endHour) {
            duration += endMin - startMin;
        } else {
            if (startMin > endMin) {
                duration -= (startMin - endMin); // Example: 10:50, 11:10
            } else {
                duration += (endMin - startMin); // Example: 10:10, 11:50
            }
        }
        return duration;
    }

    public static String findEndTime(String start, int duration) {
        int[] startTime = convertToNumber(start);
        int min = startTime[1] + duration % 60;
        int hr = startTime[0] + (duration / 60) + (min / 60);
        min %= 60;
        StringBuilder endTime = new StringBuilder();

        if (hr < 10) {
            endTime.append("0");    // for single digit formatting
        }
        endTime.append(hr);

        endTime.append(':');

        if (min <  10) {
            endTime.append("0");    // for single digit formatting
        }
        endTime.append(min);

        return endTime.toString();
    }
}
