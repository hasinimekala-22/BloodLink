
public class Donor {

    private int donorId;
    private String name;
    private String bloodGroup;
    private int age;
    private String phone;
    private boolean available;

    public Donor(int donorId, String name, String bloodGroup,
                 int age, String phone, boolean available) {
        this.donorId = donorId;
        this.name = name;
        this.bloodGroup = bloodGroup;
        this.age = age;
        this.phone = phone;
        this.available = available;
    }

    public int getDonorId() {
        return donorId;
    }

    public String getName() {
        return name;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public int getAge() {
        return age;
    }

    public String getPhone() {
        return phone;
    }

    public boolean isAvailable() {
        return available;
    }

    public void displayDetails() {
        System.out.printf("%-8d %-20s %-12s %-6d %-15s %-10s%n",
                donorId, name, bloodGroup, age, phone,
                available ? "Yes" : "No");
    }
}

