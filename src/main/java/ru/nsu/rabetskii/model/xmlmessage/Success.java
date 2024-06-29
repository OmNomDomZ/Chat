package ru.nsu.rabetskii.model.xmlmessage;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "success")
public class Success {
    private Users users;

    public Success() {
    }

    @XmlElement(name = "users")
    public Users getUsers() {
        return users;
    }

    public void setUsers(Users users) {
        this.users = users;
    }

    @XmlRootElement(name = "users")
    public static class Users {
        private List<User> users;

        public List<User> getUsers() {
            return users;
        }

        @XmlElement(name = "user")
        public void setUsers(List<User> users) {
            this.users = users;
        }
    }

    @XmlRootElement(name = "user")
    public static class User {
        private String name;

        @XmlElement(name = "name")
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
