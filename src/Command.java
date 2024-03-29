import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.sql.*;

/**
 * Created by kamil on 28.05.17.
 */
public abstract class Command {
    protected String answerOk = "{ \"status\": \"OK\"}";
    protected String answerError = "{ \"status\": \"ERROR\"}";
    protected JSONObject result;
    protected String name;


    public Command() {}

    public boolean isCommand(String s) {
        if(s.equals(name))
            return true;
        return false;
    }
    public abstract JSONObject command(JSONObject objJSON) throws SQLException, JSONException;

    protected String getRole(String login, String password) throws SQLException {
        Connection connection= DataBase.getInstance().getConnection();
        PreparedStatement prepStmt = connection.prepareStatement(
        "SELECT * FROM users " +
             "WHERE login = ? AND password = ?;");
        prepStmt.setString(1, login);
        prepStmt.setString(2, password);
        ResultSet rs = prepStmt.executeQuery();
        rs.next();
        return rs.getString("role");
    }

    protected void authorizationOrganizer(String login, String password) throws SQLException {
        String role = new String();
        try{
            role=getRole(login,password);
        }catch (SQLException e){
            e.setNextException(new SQLException("No user found:"+login));
        }
        if (!role.equals("organizer"))
            throw new SQLException("No permission for "+login);
    }
    protected void authorizationUser(String login, String password) throws SQLException {
        String role=new String();
        try{
            role=getRole(login,password);
        }catch (SQLException e){
            e.setNextException(new SQLException("No user found:"+login));
        }
        if (!role.equals("organizer")&&!role.equals("users"))
            throw new SQLException("No permission for "+login);
    }

    private void addRow(ResultSet rs,JSONArray arrJSON, String ... names) throws SQLException, JSONException {
        JSONObject row = new JSONObject();
        for(int i=0;i<names.length;i++){
            row.put(names[i],rs.getString(i+1));
        }
        arrJSON.put(row);
    }

    protected JSONArray getArray(ResultSet rs,int limit, String ... names) throws SQLException, JSONException {
        JSONArray arrJSON=new JSONArray();
        if(limit==0)
            while (rs.next())
                addRow(rs,arrJSON,names);
        else
            for (int i = 0; i < limit && rs.next(); i++)
                addRow(rs,arrJSON,names);
        return arrJSON;
    }

    protected boolean isRowInTalk(String talk, String status) throws SQLException {
        Connection connection=DataBase.getInstance().getConnection();
        PreparedStatement prepStmt = connection.prepareStatement(
        "SELECT * " +
            "FROM talk " +
            "WHERE id_talk = ?" +
            "AND status= ? ;"
        );
        prepStmt.setString(1, talk);
        prepStmt.setString(2,status);
        ResultSet rs = prepStmt.executeQuery();
        return rs.next();
    }
}
