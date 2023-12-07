import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class IRoadTrip {
    //country information stored in data structure
    private Map<String, Map<String, Integer>> countries_land_neighbours;
    private Map<String, String> country_name_to_code;
    private Map<String, String> country_code_to_name;
    private Map<String, Map<String, Integer>> distances;

    //constructor reads from files
    public IRoadTrip(String[] args) {
        countries_land_neighbours = new HashMap<>();
        distances = new HashMap<>();
        country_name_to_code = new HashMap<>();
        country_code_to_name = new HashMap<>();

        //reads file information
        readStatename(args[2]);
        readBorders(args[0]);
        readCapDist(args[1]);
    }

    public static String removeTextBetweenParentheses(String input) {
        //checks for parenthesis to avoid contradicting country names

        StringBuilder result = new StringBuilder(); //initialized to store result
        boolean insideParentheses = false; //checks whether char is in parenthesis

        char[] charArray = input.toCharArray();
        int length = charArray.length;
        for (int i = 0; i < length; i++) {
            char c = charArray[i];
            //checks for opening and closing parenthesis chars
            if (c == '(') {
                insideParentheses = true;
            }
            else if (c == ')' && insideParentheses) {
                insideParentheses = false;
            }
            else if (!insideParentheses) {
                result.append(c);
            }
        }
        return result.toString().trim();
    }

    private void readBorders(String filePath) {
        //read border information
        String curr_line;

        try (BufferedReader buffered_reader = new BufferedReader(new FileReader(filePath))) {
           //goes through each line of the file and splits it against '='

            while ((curr_line = buffered_reader.readLine()) != null) {
                String[] country_data = curr_line.split("=");

                //checks for valid country info
                if (country_data.length > 1) {

                    //gets country name, and checks against phrase in the parenthesis
                    String country_name = removeTextBetweenParentheses(country_data[0].trim()); //for whitespace
                    String country_code = getCountryCode(country_name);

                    //checks for border info in file and splits it against ';'
                    String[] border_info = country_data[1].split(";");
                    Map<String, Integer> border_map = new HashMap<>();

                    //gets country name from border info while going through all the info in the file
                    for (int i = 0; i < border_info.length; i++) {
                        String border = border_info[i];
                        String inputText = border;
                        StringBuilder alpha = new StringBuilder();
                        char[] charArray = inputText.toCharArray();

                        for (int j = 0; j < charArray.length; j++) {
                            char c = charArray[j];
                            if (Character.isDigit(c)) {
                                break;
                            }
                            alpha.append(c);
                        }

                        //gets country code for border country
                        String countryName = alpha.toString();
                        String distance = "0"; // default is 0
                        countryName = removeTextBetweenParentheses(countryName);

                        //checks against invalid border countries
                        String border_country = getCountryCode(countryName);
                        if ( border_country == null ){
                            continue;
                        }
                        //parse distance and populate border info to map
                        Integer border_length = Integer.valueOf(distance);
                        border_map.put(border_country, border_length);

                    }
                    if (!border_map.isEmpty()) {
                        countries_land_neighbours.put(country_code, border_map);
                    }
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private String getCountryCode (String country) {
        //reads country code from mapping
        return country_name_to_code.get(country);
    }

    private void readCapDist(String fileName) {
        //read capital distances from file

        //goes through each line of the file and ignores the first line due to irrelevant information
        try (BufferedReader buffered_reader = new BufferedReader(new FileReader(fileName))) {
            String curr_line;
            int line_no=0;
            while ((curr_line = buffered_reader.readLine()) != null) {
                line_no++;
                if (line_no == 1) {
                    continue;
                }
                //splits lines against ','
                String[] parts = curr_line.split(",");
                //gets country information for the two border countries and distances
                String border_country1 = parts[1].trim();
                String border_country2 = parts[3].trim();
                String border_length  = parts[4].trim();

                //checks for possible land route between the countries
                if ( ! checkLandRoutePossiblity(border_country1, border_country2) ) {
                    continue;
                }
                //gets existing distance map for the first border country
                HashMap<String,Integer>  temp = (HashMap<String, Integer>) distances.get(border_country1);
                //if the distance map doesn't exist, then a new one is created
                if ( temp == null ) {
                    temp = new HashMap<>();
                }
                //populate the new distance map
                temp.put(border_country2, Integer.valueOf(border_length));
                distances.put(border_country1,temp);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean checkLandRoutePossiblity(String border_country1, String border_country2) {
        //checks for possible land route between countries
        HashMap<String,Integer>  temp = (HashMap<String, Integer>) countries_land_neighbours.get(border_country1);

        //checks for border information for first bordering country
        if ( temp == null ) {
            return false ;
        }
        //checks if the second border country is a valid border country for the first country
        if ( ! temp.containsKey(border_country2) ) {
            return false ;
        }
        return true ;
    }

    private void readStatename(String filePath) {
        //reads state name from file
        try (BufferedReader buffered_reader = new BufferedReader(new FileReader(filePath))) {
            String curr_line;

            //goes through each line of the file and splits it against '\t'
            while ((curr_line = buffered_reader.readLine()) != null) {
                //gets country information
                String[] border_info = curr_line.split("\t");
                String country_code = border_info[1].trim();
                String country = removeTextBetweenParentheses(border_info[2].trim());
                String end_date = border_info[4].trim();

                //checks for the latest date and populate the mappings with the country name and code
                if (end_date.equals("2020-12-31")) {
                    country_name_to_code.put(country, country_code);
                    country_code_to_name.put(country_code,country);
                }
            }
        }

        catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void userInput() {
        //gets country name from user, and checks against it
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("Enter the name of the first country (type EXIT to quit): ");
            String country1 = scanner.nextLine().trim();

            if (country1.equalsIgnoreCase("EXIT")) {
                break;
            }

            //error check against invalid input
            if (!country_name_to_code.containsKey(country1)) {
                System.out.println("Invalid country name. Please enter a valid country name.");
                continue;
            }

            System.out.print("Enter the name of the second country (type EXIT to quit): ");
            String country2 = scanner.nextLine().trim();

            if (country2.equalsIgnoreCase("EXIT")) {
                break;
            }

            //error check against invalid input
            if (!country_name_to_code.containsKey(country2)) {
                System.out.println("Invalid country name. Please enter a valid country name.");
                continue;
            }

            //checks for the shortest path between the two countries
            List<String> path = findPath(country1, country2);

            //returns the path and distances
            if (!path.isEmpty()) {
                for (int i = 0; i < path.size() - 1; i++) {
                    String curr_country = path.get(i);
                    String next_country = path.get(i + 1);
                    int distance = distances.get(curr_country).get(next_country);
                    System.out.println("* " + country_code_to_name.get(curr_country) + " --> " + country_code_to_name.get(next_country) + " (" + distance + " km.)");
                }
            }
        }
        //prevents leaks
        scanner.close();
    }

    public List<String> findPath(String country1, String country2) {
        //finds the shortest distance between countries through all possible paths
        List<String> new_path = new ArrayList<>();

        //checks the validity of country names
        if (!country_name_to_code.containsKey(country1) || !country_name_to_code.containsKey(country2)) {
            return new_path;
        }

        //gets country codes
        String country1_code = getCountryCode(country1);
        String country2_code = getCountryCode(country2);

        //checks for information for the two countries
        if (!distances.containsKey(country1_code) || !distances.containsKey(country2_code)) {
            return new_path;
        }
        //uses dijkstra's algorithm to get the shortest path between the two countries
        Map<String, String> previous = dijkstraPath(country1_code, country2_code);

        //checks for a path between countries
        if (!previous.containsKey(country2_code)) {
            return new_path;
        }

        String current = country2_code;
        while (current != null) {
            new_path.add(current);
            current = previous.get(current);
        }

        //reverse lust to return correct order
        Collections.reverse(new_path);
        return new_path;
    }

    private Map<String, String> dijkstraPath(String start, String end) {
        //used in findPath to get the shortest distance

        //initialize data structures
        Map<String, Integer> distanceMap = new HashMap<>();
        Map<String, String> previous = new HashMap<>();
        Set<String> visited = new HashSet<>();
        PriorityQueue<String> priority_queue = new PriorityQueue<>(Comparator.comparingInt(distanceMap::get));

        if (distances == null || distances.isEmpty()) {
            return previous;
        }

        //set distance for the first country to 0 and add to priority queue
        distanceMap.put(start, 0);
        priority_queue.add(start);

        while (!priority_queue.isEmpty()) {
            String current = priority_queue.poll();
            //check to see if the second country has been found through this path
            if (current.equals(end)) {
                return previous;
            }

            if (distances.containsKey(current)) {
                if (!visited.contains(current)) {
                    visited.add(current);
                    Map<String, Integer> neighbors = distances.get(current);
                    //finds the neighboring countries and updates distances accordingly
                    if (neighbors != null) {
                        for (Map.Entry<String, Integer> neighbor : neighbors.entrySet()) {
                            String neighboring_country = neighbor.getKey();
                            int total_dist  = distanceMap.get(current) + neighbor.getValue();
                            if (!distanceMap.containsKey(neighboring_country) || total_dist < distanceMap.get(neighboring_country)) {
                                distanceMap.put(neighboring_country, total_dist);
                                previous.put(neighboring_country, current);
                                priority_queue.add(neighboring_country);
                            }
                        }
                    }
                }
            }
        }
        //returns the map that has the prvious country in the quickest and shortest path
        return previous;
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            System.exit(1);
        }

        IRoadTrip roadTrip = new IRoadTrip(args);
        roadTrip.userInput();
    }
}