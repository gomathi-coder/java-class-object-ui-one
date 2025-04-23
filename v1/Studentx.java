public class Studentx {

    String name;
    int age;
    float weight;
    int classNumber;
    String section;
    int rollNumber;

    public void tellAboutYourself() {
        System.out.println("Name: " + name);
        System.out.println("Age: " + age);
        System.out.println("Weight: " + weight);
        System.out.println("Class: " + classNumber);
        System.out.println("Section: " + section);
    }

    public void tellYourClass() {
        System.out.println("Class: " + classNumber + ", Section: " + section);
    }

    public int setYourClass(int classv) {
        this.classNumber = classv;
        return this.classNumber;
    }

}