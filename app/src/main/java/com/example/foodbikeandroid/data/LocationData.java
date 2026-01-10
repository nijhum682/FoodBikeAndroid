package com.example.foodbikeandroid.data;

import com.example.foodbikeandroid.data.model.Division;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocationData {

    private static final Map<String, Division> divisions = new HashMap<>();
    private static final Map<String, String> divisionPrefixes = new HashMap<>();

    static {
        divisions.put("Dhaka", new Division("Dhaka", "DH", Arrays.asList(
                "Dhaka", "Gazipur", "Narayanganj", "Tangail", "Manikganj",
                "Munshiganj", "Narsingdi", "Faridpur", "Gopalganj", "Madaripur",
                "Rajbari", "Shariatpur", "Kishoreganj"
        )));

        divisions.put("Chittagong", new Division("Chittagong", "CH", Arrays.asList(
                "Chittagong", "Cox's Bazar", "Comilla", "Feni", "Brahmanbaria",
                "Rangamati", "Bandarban", "Khagrachhari", "Noakhali", "Lakshmipur", "Chandpur"
        )));

        divisions.put("Sylhet", new Division("Sylhet", "SY", Arrays.asList(
                "Sylhet", "Moulvibazar", "Habiganj", "Sunamganj"
        )));

        divisions.put("Rajshahi", new Division("Rajshahi", "RJ", Arrays.asList(
                "Rajshahi", "Bogra", "Pabna", "Sirajganj", "Natore",
                "Nawabganj", "Naogaon", "Joypurhat"
        )));

        divisions.put("Khulna", new Division("Khulna", "KH", Arrays.asList(
                "Khulna", "Jessore", "Satkhira", "Bagerhat", "Narail",
                "Magura", "Kushtia", "Chuadanga", "Meherpur", "Jhenaidah"
        )));

        divisions.put("Barisal", new Division("Barisal", "BA", Arrays.asList(
                "Barisal", "Bhola", "Patuakhali", "Pirojpur", "Jhalokathi", "Barguna"
        )));

        divisions.put("Rangpur", new Division("Rangpur", "RP", Arrays.asList(
                "Rangpur", "Dinajpur", "Kurigram", "Nilphamari", "Lalmonirhat",
                "Gaibandha", "Thakurgaon", "Panchagarh"
        )));

        divisions.put("Mymensingh", new Division("Mymensingh", "MY", Arrays.asList(
                "Mymensingh", "Jamalpur", "Sherpur", "Netrokona"
        )));

        divisionPrefixes.put("Dhaka", "DH");
        divisionPrefixes.put("Chittagong", "CH");
        divisionPrefixes.put("Sylhet", "SY");
        divisionPrefixes.put("Rajshahi", "RJ");
        divisionPrefixes.put("Khulna", "KH");
        divisionPrefixes.put("Barisal", "BA");
        divisionPrefixes.put("Rangpur", "RP");
        divisionPrefixes.put("Mymensingh", "MY");
    }

    public static List<String> getAllDivisions() {
        return new ArrayList<>(Arrays.asList(
                "Filter by Division", "Dhaka", "Chittagong", "Sylhet", "Rajshahi",
                "Khulna", "Barisal", "Rangpur", "Mymensingh"
        ));
    }

    public static List<String> getDistrictsForDivision(String division) {
        if (division == null || division.equals("Filter by Division")) {
            return Arrays.asList("Filter by District");
        }
        Division div = divisions.get(division);
        if (div != null) {
            List<String> districts = new ArrayList<>();
            districts.add("Filter by District");
            districts.addAll(div.getDistricts());
            return districts;
        }
        return Arrays.asList("Filter by District");
    }

    public static String getDivisionPrefix(String division) {
        return divisionPrefixes.getOrDefault(division, "XX");
    }

    public static Division getDivision(String name) {
        return divisions.get(name);
    }

    public static String generateRestaurantId(String division, int number) {
        String prefix = getDivisionPrefix(division);
        return String.format("%s%03d", prefix, number);
    }
}
