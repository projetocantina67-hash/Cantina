import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestJackson {
    public static class User {
        @JsonIgnore
        private String senha;
        
        public String getSenha() { return senha; }
        
        @JsonProperty("senha")
        public void setSenha(String senha) { this.senha = senha; }
    }

    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        User u = mapper.readValue("{\"senha\":\"123456\"}", User.class);
        System.out.println("Deserialized: " + u.getSenha());
        System.out.println("Serialized: " + mapper.writeValueAsString(u));
    }
}
