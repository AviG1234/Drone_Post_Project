package UserManagement;

import java.time.LocalDate;
import java.util.ArrayList;

public class Person {
    private String personalId;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String street;
    private String building;
    private String city;
    private String zipCode;
    private String email;
    private String telephone;

    /**
     * Create the instance of Users.Person. The person shall have mandatory fields
     * @param personDetails - ordered list of data: personalId, firstName, lastName, dateOfBirth,
     *                        street, building, city, zipCode, email, telephone. All the field values are mandatory,
     *                        except: firstName and building
     */
    public Person (ArrayList<String> personDetails){
        try {
            setPersonalId(personDetails.get(0));
            setFirstName(personDetails.get(1));
            setLastName(personDetails.get(2));
            setDateOfBirth(personDetails.get(3));
            setStreet(personDetails.get(4));
            setBuilding(personDetails.get(5));
            setCity(personDetails.get(6));
            setZipCode(personDetails.get(7));
            setEmail(personDetails.get(8));
            setTelephone(personDetails.get(9));
        } catch (IllegalArgumentException e){
            e.printStackTrace();
        }
    }
    public String getPersonalId() {
        return personalId;
    }
    public String getFirstName() {
        return firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public String getFullName() { return getFirstName() + " " + getLastName(); }
    public String getBuilding() {
        if (building != null)
            return building;
        else return "";
    }
    public String getCity() {
        return city;
    }
    public String getStreet() {
        return street;
    }
    public String getZipCode() {
        return zipCode;
    }
    public String getFullAddress(){
        return street + " St, " + (building != null ? building + ", " : "") + city + ", " + zipCode;
    }
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }
    public String getEmail() {return email; }
    public String getTelephone() {
        return telephone;
    }

    public void setPersonalId (String personalId) throws IllegalArgumentException{
        boolean isDigit = false;
        for(int i = 0; i< personalId.length(); ++i) {
            isDigit = Character.isDigit(personalId.charAt(i));
            if (!isDigit) break;
        }
        if (personalId.isEmpty() || personalId.length() > 10 || !isDigit)
                throw new IllegalArgumentException("Fill in all the mandatory fields correctly");
            this.personalId = personalId;
    }
    public void setFirstName(String firstName) throws IllegalArgumentException{
            if (firstName.length() > 30)
                throw new IllegalArgumentException("Fill in all the mandatory fields correctly");
            this.firstName = firstName;
    }
    public void setLastName(String lastName) throws IllegalArgumentException{
            if (lastName.isEmpty() || lastName.length() > 30)
                throw new IllegalArgumentException("Fill in all the mandatory fields correctly");
            this.lastName = lastName;
    }
    public void setDateOfBirth (String dateOfBirth) throws IllegalArgumentException {
        LocalDate dateOfBirthParsed = LocalDate.parse(dateOfBirth);
            if (dateOfBirthParsed.isAfter(LocalDate.now()))
                throw new IllegalArgumentException("Fill in all the mandatory fields correctly");
            this.dateOfBirth = dateOfBirthParsed;
    }
    public void setBuilding(String building) throws IllegalArgumentException {
        if (building.length() > 5)
            throw new IllegalArgumentException("Fill in all the mandatory fields");
        this.building = building;
    }
    public void setCity(String city) throws IllegalArgumentException {
        if (city.isEmpty() || city.length() > 30)
            throw new IllegalArgumentException("Fill in all the mandatory fields");
        this.city = city;
    }
    public void setStreet(String street) throws IllegalArgumentException {
        if (street.isEmpty() || street.length() > 40)
            throw new IllegalArgumentException("Fill in all the mandatory fields");
        this.street = street;
    }
    public void setZipCode(String zipCode) throws IllegalArgumentException {
        if (zipCode.isEmpty() || zipCode.length() > 10)
            throw new IllegalArgumentException("Fill in all the mandatory fields");
        this.zipCode = zipCode;
    }
    public void setEmail (String email) throws IllegalArgumentException {
            if (email.isEmpty() || email.length() > 45)
                throw new IllegalArgumentException("Fill in all the mandatory fields correctly");
            this.email = email;
    }
    public void setTelephone(String telephone) throws IllegalArgumentException {
            if (telephone.length() > 15 || !telephone.startsWith("+"))
                throw new IllegalArgumentException("Fill in all the mandatory fields correctly");
            this.telephone = telephone;
    }
}
