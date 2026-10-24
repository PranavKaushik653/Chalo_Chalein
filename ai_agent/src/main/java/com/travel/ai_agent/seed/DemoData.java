package com.travel.ai_agent.seed;

import com.travel.ai_agent.model.Organizer;
import com.travel.ai_agent.model.Trip;
import com.travel.ai_agent.repository.OrganizerRepository;
import com.travel.ai_agent.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "chalo.seed-demo-data", havingValue = "true", matchIfMissing = true)
public class DemoData implements CommandLineRunner {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    private final TripRepository trips;
    private final OrganizerRepository organizers;

    @Override
    public void run(String... args) {
        if (trips.count() > 0) {
            log.info("Trips table not empty, skipping demo seed.");
            return;
        }

        Organizer himalayan = org("Himalayan Nomads", "himalayan.nomads", "VERIFIED");
        Organizer tribe = org("Backpack Tribe", "backpack.tribe", "VERIFIED");
        Organizer coast = org("Coastline Collective", "coastline.collective", "VERIFIED");
        Organizer desert = org("Desert Compass", "desert.compass", "VERIFIED");
        Organizer pending = org("Shady Trails", "shady.trails", "PENDING");

        // organizer, code, title, destination, region, from, startInDays, days,
        // totalSeats, seatsLeft, price, activities, description

        trip(himalayan, "KSL-01", "Kasol & Kheerganga Trek Weekender",
                "Kasol", "Himachal Pradesh", "Delhi",
                16, 3, 20, 9, 4999,
                "trekking,camping,cafe hopping,bonfire",
                "Overnight bus from Delhi, easy trek to Kheerganga hot springs, riverside camp and bonfire night.");

        trip(tribe, "KSL-02", "Kasol Budget Backpackers",
                "Kasol", "Himachal Pradesh", "Delhi",
                9, 3, 25, 1, 3799,
                "trekking,cafe hopping,sightseeing",
                "No-frills hostel stay, Kasol and Manikaran sightseeing and a short Chalal village trek.");

        trip(himalayan, "TRN-01", "Triund Sunset Trek + Camp",
                "McLeodganj", "Himachal Pradesh", "Delhi",
                23, 3, 18, 12, 3999,
                "trekking,camping,stargazing",
                "Beginner friendly Triund ridge trek, tent stay under the stars and sunrise over the Dhauladhars.");

        trip(himalayan, "SPT-01", "Winter Spiti Expedition",
                "Spiti Valley", "Himachal Pradesh", "Delhi",
                55, 8, 14, 5, 18500,
                "road trip,monasteries,photography,snow,stargazing",
                "Key Monastery, Chandratal in snow, Kibber and Langza with heated homestays. Sub-zero nights.");

        trip(himalayan, "TIR-01", "Tirthan Valley Offbeat Escape",
                "Tirthan Valley", "Himachal Pradesh", "Delhi",
                30, 4, 16, 10, 6500,
                "trekking,riverside camping,fishing,nature walks",
                "Great Himalayan National Park trails, trout fishing and riverside stay away from the crowds.");

        trip(tribe, "RSK-01", "Rishikesh Rafting & Bungee Weekend",
                "Rishikesh", "Uttarakhand", "Delhi",
                12, 3, 24, 2, 4499,
                "river rafting,bungee jumping,camping,yoga,cliff jumping",
                "16 km Ganga rafting, India's highest bungee jump, beach camping and sunrise yoga.");

        trip(himalayan, "AUL-01", "Auli Ski & Snow Trail",
                "Auli", "Uttarakhand", "Delhi",
                80, 4, 16, 11, 9999,
                "skiing,snow,trekking,bonfire",
                "Beginner ski lessons, gondola ride and snow trek with views of Nanda Devi.");

        trip(coast, "GOA-01", "Goa New Year Beach Party Pass",
                "Goa", "Goa", "Mumbai",
                70, 4, 30, 14, 8499,
                "beach parties,nightlife,water sports,cafe hopping",
                "Beachside stay, 2 party nights, water sports, and a New Year's Eve gala entry.");

        trip(coast, "GKR-01", "Gokarna Beach Trek & Chill",
                "Gokarna", "Karnataka", "Bengaluru",
                20, 3, 20, 6, 4200,
                "beach trek,camping,beach,bonfire",
                "Trek across Om, Kudle, Half Moon and Paradise beaches with a camp night on the sand.");

        trip(tribe, "HMP-01", "Hampi Ruins & Bouldering",
                "Hampi", "Karnataka", "Bengaluru",
                35, 3, 15, 8, 5500,
                "bouldering,heritage walk,cycling,sunset points",
                "Cycle through ruins, boulder with local guides and watch sunset from Matanga Hill.");

        trip(coast, "CRG-01", "Coorg Coffee Trails & Rafting",
                "Coorg", "Karnataka", "Bengaluru",
                27, 3, 18, 0, 5800,
                "coffee plantation,river rafting,trekking,camping",
                "Plantation stay, Barapole rafting and a sunrise trek to Tadiandamol base.");

        trip(coast, "MNR-01", "Munnar Tea Hills Getaway",
                "Munnar", "Kerala", "Bengaluru",
                52, 4, 16, 9, 7800,
                "tea gardens,trekking,nature walks,camping",
                "Tea estate walks, Meesapulimala trek option, campfire and a Kolukkumalai sunrise jeep ride.");

        trip(tribe, "MGH-01", "Meghalaya Living Root Bridges",
                "Meghalaya", "Meghalaya", "Guwahati",
                40, 6, 20, 7, 17500,
                "trekking,canyoning,caving,living root bridges,kayaking",
                "Double decker root bridge, Dawki kayaking, Mawlynnong and cave exploring.");

        trip(desert, "JSL-01", "Jaisalmer Desert Camp Weekender",
                "Jaisalmer", "Rajasthan", "Delhi",
                62, 4, 22, 13, 8999,
                "desert camping,camel safari,dune bashing,stargazing,folk music",
                "Sam sand dunes camp, camel safari, folk music night and Jaisalmer fort walk.");

        trip(desert, "VNS-01", "Varanasi Ghats & Ganga Aarti",
                "Varanasi", "Uttar Pradesh", "Delhi",
                18, 3, 25, 15, 5200,
                "heritage walk,spiritual,food walk,boat ride",
                "Sunrise boat ride, evening aarti, street food walk and Sarnath visit.");

        trip(coast, "AND-01", "Andaman Island Hopper",
                "Andaman", "Andaman & Nicobar", "Chennai",
                58, 6, 15, 6, 28500,
                "scuba diving,snorkeling,beaches,kayaking",
                "Havelock and Neil islands, Radhanagar beach, discover scuba and mangrove kayaking.");

        trip(tribe, "BAL-01", "Bali Backpackers Squad",
                "Bali", "Indonesia", "Delhi",
                75, 6, 12, 3, 42000,
                "surfing,temple visits,beach clubs,waterfalls,scooter rides",
                "Ubud, Canggu and Uluwatu with surf lessons, waterfall hunting and a beach club night.");

        trip(pending, "SHT-01", "Cheap Manali Blast",
                "Manali", "Himachal Pradesh", "Delhi",
                14, 3, 30, 30, 2999,
                "trekking,camping,nightlife",
                "Unverified organizer. Must be filtered out of every result.");

        log.info("Seeded {} demo trips for the travel agent.", trips.count());
    }

    private Organizer org(String name, String insta, String status) {
        return organizers.save(Organizer.builder()
                .businessName(name)
                .instagramHandle(insta)
                .verificationStatus(status)
                .build());
    }

    private void trip(Organizer org, String code, String title, String dest,
                      String region, String from, int startInDays, int days,
                      int totalSeats, int seatsLeft, int price,
                      String activities, String description) {

        LocalDate start = LocalDate.now(IST).plusDays(startInDays);
        LocalDate end = start.plusDays(days - 1L);

        trips.save(Trip.builder()
                .organizer(org)
                .tripCode(code)
                .title(title)
                .destination(dest)
                .region(region)
                .departureCity(from)
                .startDate(start.atTime(6, 0).atZone(IST).toInstant())
                .endDate(end.atTime(20, 0).atZone(IST).toInstant())
                .totalSeats(totalSeats)
                .availableSeats(seatsLeft)
                .tokenAmount(BigDecimal.valueOf(Math.round(price * 0.2 / 100.0) * 100L))
                .totalAmount(BigDecimal.valueOf(price))
                .activities(activities)
                .description(description)
                .build());
    }
}
