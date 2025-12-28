package ens.edupath.note.repository;

import ens.edupath.note.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByStudentId(String studentId);
    List<Note> findByModuleId(Long moduleId);
    List<Note> findByStudentIdAndModuleId(String studentId, Long moduleId);
    
    @Query("SELECT AVG(n.score / n.maxScore * 100) FROM Note n WHERE n.studentId = :studentId AND n.moduleId = :moduleId")
    Double calculateAveragePercentage(@Param("studentId") String studentId, @Param("moduleId") Long moduleId);
    
    @Query("SELECT SUM(n.score) FROM Note n WHERE n.studentId = :studentId AND n.moduleId = :moduleId")
    Double calculateTotalScore(@Param("studentId") String studentId, @Param("moduleId") Long moduleId);
    
    @Query("SELECT SUM(n.maxScore) FROM Note n WHERE n.studentId = :studentId AND n.moduleId = :moduleId")
    Double calculateTotalMaxScore(@Param("studentId") String studentId, @Param("moduleId") Long moduleId);
    
    @Query("SELECT COUNT(n) FROM Note n WHERE n.studentId = :studentId AND n.moduleId = :moduleId")
    Long countByStudentIdAndModuleId(@Param("studentId") String studentId, @Param("moduleId") Long moduleId);
}


