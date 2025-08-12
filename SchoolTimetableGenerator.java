import java.util.*;

class Teacher {
    String name;
    Set<String> qualifiedSubjects;

    Teacher(String name, Set<String> qualifiedSubjects) {
        this.name = name;
        this.qualifiedSubjects = qualifiedSubjects;
    }
}

class ClassRoom {
    String name;
    Map<String, Integer> subjectPeriods; // subject -> required periods

    ClassRoom(String name, Map<String, Integer> subjectPeriods) {
        this.name = name;
        this.subjectPeriods = subjectPeriods;
    }
}

class PeriodAssignment {
    String subject;
    Teacher teacher;

    PeriodAssignment(String subject, Teacher teacher) {
        this.subject = subject;
        this.teacher = teacher;
    }
}

public class SchoolTimetableGenerator {
    static final int DAYS = 5;
    static final int PERIODS_PER_DAY = 6;
    static final String[] DAY_NAMES = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};

    List<ClassRoom> classes;
    List<Teacher> teachers;
    Map<String, Map<Integer, PeriodAssignment[]>> timetable; // class -> day -> periods

    public SchoolTimetableGenerator(List<ClassRoom> classes, List<Teacher> teachers) {
        this.classes = classes;
        this.teachers = teachers;
        this.timetable = new HashMap<>();
    }

    public void generateTimetable() {
        // Initialize empty timetable
        for (ClassRoom c : classes) {
            Map<Integer, PeriodAssignment[]> classSchedule = new HashMap<>();
            for (int d = 0; d < DAYS; d++) {
                classSchedule.put(d, new PeriodAssignment[PERIODS_PER_DAY]);
            }
            timetable.put(c.name, classSchedule);
        }

        // Track teacher assignments: teacher -> day -> period
        Map<String, boolean[][]> teacherBusy = new HashMap<>();
        for (Teacher t : teachers) {
            teacherBusy.put(t.name, new boolean[DAYS][PERIODS_PER_DAY]);
        }

        // For each class, assign subjects and teachers to periods
        for (ClassRoom c : classes) {
            List<String> subjects = new ArrayList<>(c.subjectPeriods.keySet());
            Map<String, Integer> remainingPeriods = new HashMap<>(c.subjectPeriods);

            for (String subject : subjects) {
                int periodsNeeded = remainingPeriods.get(subject);
                Teacher assignedTeacher = null;
                for (Teacher t : teachers) {
                    if (t.qualifiedSubjects.contains(subject)) {
                        assignedTeacher = t;
                        break;
                    }
                }
                if (assignedTeacher == null) throw new RuntimeException("No teacher for subject " + subject);

                // Assign periods for this subject
                int assigned = 0;
                outer:
                for (int d = 0; d < DAYS; d++) {
                    for (int p = 0; p < PERIODS_PER_DAY; p++) {
                        if (timetable.get(c.name).get(d)[p] == null && !teacherBusy.get(assignedTeacher.name)[d][p]) {
                            timetable.get(c.name).get(d)[p] = new PeriodAssignment(subject, assignedTeacher);
                            teacherBusy.get(assignedTeacher.name)[d][p] = true;
                            assigned++;
                            if (assigned == periodsNeeded) break outer;
                        }
                    }
                }
                if (assigned < periodsNeeded) throw new RuntimeException("Not enough slots for " + subject + " in " + c.name);
            }
        }
    }

    public boolean validateTimetable() {
        // Check all constraints
        for (ClassRoom c : classes) {
            Map<String, Integer> subjectCount = new HashMap<>();
            for (int d = 0; d < DAYS; d++) {
                for (int p = 0; p < PERIODS_PER_DAY; p++) {
                    PeriodAssignment pa = timetable.get(c.name).get(d)[p];
                    if (pa != null) {
                        subjectCount.put(pa.subject, subjectCount.getOrDefault(pa.subject, 0) + 1);
                        if (!pa.teacher.qualifiedSubjects.contains(pa.subject)) return false;
                    }
                }
            }
            for (String subject : c.subjectPeriods.keySet()) {
                if (subjectCount.getOrDefault(subject, 0) != c.subjectPeriods.get(subject)) return false;
            }
        }
        // Check teacher double-booking
        for (Teacher t : teachers) {
            for (int d = 0; d < DAYS; d++) {
                for (int p = 0; p < PERIODS_PER_DAY; p++) {
                    int count = 0;
                    for (ClassRoom c : classes) {
                        PeriodAssignment pa = timetable.get(c.name).get(d)[p];
                        if (pa != null && pa.teacher.name.equals(t.name)) count++;
                    }
                    if (count > 1) return false;
                }
            }
        }
        return true;
    }

    public void displayTimetable() {
        for (ClassRoom c : classes) {
            System.out.println("Timetable for " + c.name + ":");
            for (int d = 0; d < DAYS; d++) {
                System.out.println("  " + DAY_NAMES[d] + ":");
                for (int p = 0; p < PERIODS_PER_DAY; p++) {
                    PeriodAssignment pa = timetable.get(c.name).get(d)[p];
                    if (pa != null) {
                        System.out.printf("    Period %d: %s (%s)\n", p + 1, pa.subject, pa.teacher.name);
                    } else {
                        System.out.printf("    Period %d: Free\n", p + 1);
                    }
                }
            }
            System.out.println();
        }
    }

    // Sample test case
    public static void main(String[] args) {
        // Define teachers
        Teacher t1 = new Teacher("Alice", new HashSet<>(Arrays.asList("Math", "Science")));
        Teacher t2 = new Teacher("Bob", new HashSet<>(Arrays.asList("English", "History")));
        Teacher t3 = new Teacher("Carol", new HashSet<>(Arrays.asList("Math", "English")));

        // Define classes
        Map<String, Integer> class6A = new HashMap<>();
        class6A.put("Math", 5);
        class6A.put("Science", 4);
        class6A.put("English", 3);

        Map<String, Integer> class6B = new HashMap<>();
        class6B.put("Math", 5);
        class6B.put("History", 4);
        class6B.put("English", 3);

        List<ClassRoom> classes = Arrays.asList(
            new ClassRoom("Class 6A", class6A),
            new ClassRoom("Class 6B", class6B)
        );
        List<Teacher> teachers = Arrays.asList(t1, t2, t3);

        SchoolTimetableGenerator generator = new SchoolTimetableGenerator(classes, teachers);
        generator.generateTimetable();

        if (generator.validateTimetable()) {
            System.out.println("Timetable is valid!\n");
            generator.displayTimetable();
        } else {
            System.out.println("Timetable is invalid!");
        }
    }
}
