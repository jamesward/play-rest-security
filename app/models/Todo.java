package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import play.data.validation.Constraints;

import javax.persistence.*;
import java.util.List;

@Entity
public class Todo extends Model {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(length = 1024, nullable = false)
    @Constraints.MaxLength(1024)
    @Constraints.Required
    public String value;

    @ManyToOne
    @JsonIgnore
    public User user;
    
    public Todo(User user, String value) {
        this.user = user;
        this.value = value;
    }

    public static List<Todo> findByUser(User user) {
        Finder<Long, Todo> finder = new Finder<>(Todo.class);
        return finder.where().eq("user", user).findList();
    }
}
