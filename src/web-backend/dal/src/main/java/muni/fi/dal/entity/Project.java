package muni.fi.dal.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.joda.time.DateTime;

import java.util.Objects;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Project extends BaseEntity {

    private String projId;

    private String regCode;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String title;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "author_id")
    private Author author;

    private String state;

    private DateTime dateBegin;

    private DateTime dateEnd;

    private String muniRole;

    private String investor;

    @ManyToOne(cascade = CascadeType.PERSIST)
    private Department department;

    @Column(columnDefinition = "TEXT")
    private String annotation;

    @Column(columnDefinition = "TEXT")
    private String processedAnnotation;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Project project = (Project) o;
        return getId() != null && Objects.equals(getId(), project.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
