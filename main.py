
#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
School Timetable Generator

This program generates an optimal weekly timetable for a school based on 
classes, subjects, teachers, and period requirements.
"""

### DO NOT MODIFY THE CODE BELOW THIS LINE ###

# Define the input constraints
# Classes
classes = ["Class 6A", "Class 6B", "Class 7A", "Class 7B"]

# Subjects
subjects = ["Mathematics", "Science", "English", "Social Studies", "Computer Science", "Physical Education"]

# Weekly period requirements for each class and subject
# {class_name: {subject_name: number_of_periods_per_week}}
class_subject_periods = {
    "Class 6A": {"Mathematics": 6, "Science": 6, "English": 6, "Social Studies": 6, "Computer Science": 3, "Physical Education": 3},
    "Class 6B": {"Mathematics": 6, "Science": 6, "English": 6, "Social Studies": 6, "Computer Science": 3, "Physical Education": 3},
    "Class 7A": {"Mathematics": 6, "Science": 6, "English": 6, "Social Studies": 6, "Computer Science": 4, "Physical Education": 2},
    "Class 7B": {"Mathematics": 6, "Science": 6, "English": 6, "Social Studies": 6, "Computer Science": 4, "Physical Education": 2}
}

# Teachers and their teaching capabilities
# {teacher_name: [list_of_subjects_they_can_teach]}
teachers = {
    "Mr. Kumar": ["Mathematics"],
    "Mrs. Sharma": ["Mathematics"],
    "Ms. Gupta": ["Science"],
    "Mr. Singh": ["Science", "Social Studies"],
    "Mrs. Patel": ["English"],
    "Mr. Joshi": ["English", "Social Studies"],
    "Mr. Malhotra": ["Computer Science"],
    "Mr. Chauhan": ["Physical Education"]
}

# School timing configuration
days_of_week = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday"]
periods_per_day = 6

### DO NOT MODIFY THE CODE ABOVE THIS LINE ###

import random
from collections import defaultdict

def generate_timetable():
    timetable = {day: {period: {} for period in range(1, periods_per_day + 1)} for day in days_of_week}
    class_requirements = {cls: class_subject_periods[cls].copy() for cls in classes}
    teacher_schedule = {teacher: {day: set() for day in days_of_week} for teacher in teachers}

    for day in days_of_week:
        for period in range(1, periods_per_day + 1):
            available_teachers = {teacher for teacher in teachers if period not in teacher_schedule[teacher][day]}
            random.shuffle(classes)
            for cls in classes:
                for subject, remaining in class_requirements[cls].items():
                    if remaining <= 0:
                        continue
                    qualified_teachers = [t for t in teachers if subject in teachers[t] and t in available_teachers]
                    if not qualified_teachers:
                        continue
                    teacher = random.choice(qualified_teachers)
                    timetable[day][period][cls] = (subject, teacher)
                    class_requirements[cls][subject] -= 1
                    teacher_schedule[teacher][day].add(period)
                    break
    return timetable

def display_timetable(timetable):
    print("\n----- CLASS TIMETABLES -----")
    for cls in classes:
        print(f"\nTimetable for {cls}:")
        for day in days_of_week:
            periods = []
            for period in range(1, periods_per_day + 1):
                if cls in timetable[day][period]:
                    subject, teacher = timetable[day][period][cls]
                    periods.append(f"{subject} ({teacher})")
                else:
                    periods.append("Free")
            print(f"{day}:\t" + ", ".join(periods))

    print("\n----- TEACHER TIMETABLES -----")
    for teacher in teachers:
        print(f"\nTimetable for {teacher}:")
        for day in days_of_week:
            line = []
            for period in range(1, periods_per_day + 1):
                found = False
                for cls, (subject, t) in timetable[day][period].items():
                    if t == teacher:
                        line.append(f"{subject} ({cls})")
                        found = True
                        break
                if not found:
                    line.append("Free")
            print(f"{day}:\t" + ", ".join(line))

def validate_timetable(timetable):
    class_subject_counter = defaultdict(lambda: defaultdict(int))
    teacher_day_period = defaultdict(set)

    for day in days_of_week:
        for period in range(1, periods_per_day + 1):
            assignments = timetable[day][period]
            teachers_seen = set()
            for cls, (subject, teacher) in assignments.items():
                class_subject_counter[cls][subject] += 1
                if subject not in teachers[teacher]:
                    return False, f"Teacher {teacher} is not qualified to teach {subject}."
                if teacher in teachers_seen or (teacher, period) in teacher_day_period[day]:
                    return False, f"Teacher {teacher} is double-booked on {day}, period {period}."
                teachers_seen.add(teacher)
                teacher_day_period[day].add((teacher, period))

    for cls, subject_reqs in class_subject_periods.items():
        for subject, required_count in subject_reqs.items():
            actual_count = class_subject_counter[cls][subject]
            if actual_count != required_count:
                return False, f"{cls} has {actual_count}/{required_count} periods for {subject}."

    return True, "Timetable is valid."

def main():
    print("Generating school timetable...")
    timetable = generate_timetable()
    is_valid, message = validate_timetable(timetable)
    if is_valid:
        display_timetable(timetable)
    else:
        print(f"Failed to generate valid timetable: {message}")

if __name__ == "__main__":
    main()
