package pl.zubkova.webuniversity.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import pl.zubkova.webuniversity.dao.StudentDao;
import pl.zubkova.webuniversity.entity.Student;
import pl.zubkova.webuniversity.entity.Teacher;

import java.util.*;

/**
 * @author Svetlana_Zubkova
 */
@Repository
public class StudentDaoImpl implements StudentDao {
    NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    SimpleJdbcInsert simpleJdbcInsert;
    JdbcTemplate jdbcTemplate;

    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Autowired
    public void setNamedParameterJdbcTemplate(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Autowired
    @Qualifier("Students")
    public void setSimpleJdbcInsert(SimpleJdbcInsert simpleJdbcInsert) {
        this.simpleJdbcInsert = simpleJdbcInsert;
    }

    private static final String READ_STUDENT_BY_ID = "SELECT name, surname, age, email, " +
            "specialisation FROM public.students WHERE id = :id";
    private static final String READ_ALL_STUDENTS = "select * from students";
    private static final String UPDATE_STUDENT = "UPDATE public.students SET name = :name, surname = :surname, age = :age, " +
            "email = :email, specialisation = :specialisation WHERE id = :id";

    private static final String DELETE_STUDENT = "DELETE FROM public.student WHERE id = :id";
    private static final String READ_STUDENT_BY_NAME_AND_SURNAME = "SELECT name, surname, age, email, " +
            "specialisation FROM public.students WHERE name = :name and surname = :surname";

    private static final String ADD_TEACHER_TO_STUDENT =
            "insert into students_teachers (student_id, teacher_id) values (?, ?)";
    private static final String DELETE_TEACHER_FROM_STUDENT = "delete from students_teachers where student_id = ? and teacher_id = ?";

    private static final String READ_TEACHERS_BY_STUDENT_ID =
            "SELECT* FROM teachers JOIN students_teachers ON teachers.id = teacher_id WHERE student_id = ?";

    @Override
    public void save(Student student) {
        Map<String, Object> params = getParams(student);
        int studentId = 0;
        try {
            studentId = simpleJdbcInsert.executeAndReturnKey(params).intValue();
        } catch (DataAccessException e) {
            e.getMessage();
        }
        student.setId(studentId);

    }

    @Override
    public void update(Student student) {
        if (student.getId() != 0) {
            Map<String, Object> params = getParams(student);
            params.put("id", student.getId());
            namedParameterJdbcTemplate.update(UPDATE_STUDENT, params);
        }
    }

    @Override
    public void delete(int id) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        try {
            namedParameterJdbcTemplate.update(DELETE_STUDENT, params);
        } catch (DataAccessException e) {
            e.getMessage();
        }
    }

    @Override
    public List<Student> findAllStudents() {
        List<Student> students = new ArrayList<>();
        try {
            students = jdbcTemplate.query(READ_ALL_STUDENTS, new BeanPropertyRowMapper<>(Student.class));
        } catch (DataAccessException e) {
            e.getMessage();
        }
        return students;
    }

    @Override
    public Optional<Student> findByNameAndSurname(String name, String surname) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        params.put("surname", surname);
        Student student;
        try {
            student = namedParameterJdbcTemplate.queryForObject(READ_STUDENT_BY_NAME_AND_SURNAME, params, new BeanPropertyRowMapper<>(Student.class));
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
        return Optional.of(student);
    }

    @Override
    public List<Teacher> findAllTeachersForTheStudent(int student_id) {
        List<Teacher> teachers = new ArrayList<>();
            try {
                teachers =  jdbcTemplate.query(READ_TEACHERS_BY_STUDENT_ID, new BeanPropertyRowMapper<>(Teacher.class), student_id);
            } catch (DataAccessException e) {
                e.getMessage();
            }
        return teachers;
    }

    @Override
    public void addTeacherToStudent(int teacherId, int studentId) {
        try {
            jdbcTemplate.update(ADD_TEACHER_TO_STUDENT, studentId, teacherId);
        } catch (DataAccessException e) {
            e.getMessage();
        }
    }

    @Override
    public void deleteTeacherFromStudent(int teacherId, int studentId) {
        try {
            jdbcTemplate.update(DELETE_TEACHER_FROM_STUDENT, studentId, teacherId);

        } catch (DataAccessException e) {
            e.getMessage();
        }

    }

    @Override
    public Optional<Student> findById(int studentId) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", studentId);
        Student student;
        try {
            student = namedParameterJdbcTemplate.queryForObject(READ_STUDENT_BY_ID, params, new BeanPropertyRowMapper<>(Student.class));
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
        return Optional.of(student);
    }

    private static Map<String, Object> getParams(Student student) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", student.getName());
        params.put("surname", student.getSurname());
        params.put("age", student.getAge());
        params.put("email", student.getEmail());
        params.put("specialisation", student.getSpecialisation());
        return params;
    }
}