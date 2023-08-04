package muni.fi.dal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Role extends BaseEntity {

    public Role(String name) {
        this.name = name;
    }

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @ToString.Exclude
    private User user;
}
