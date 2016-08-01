package org.mule.tools.maven.exchange.api.cs;

import static javax.ws.rs.core.Response.Status.Family.SUCCESSFUL;
import static javax.ws.rs.core.Response.Status.Family.familyOf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.logging.Log;

import org.json.JSONArray;
import org.json.JSONObject;

public abstract class AbstractMuleApi extends AbstractApi
{

    private static final String ME = "/accounts/api/me";
    private static final String LOGIN = "/accounts/login";
    private static final String UUID_PATTERN_MATCHER = "^[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$";

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String ORG_ID_HEADER = "X-ANYPNT-ORG-ID";

    private String username;
    private String password;
    protected String uri;
    protected String environment;
    protected final String businessGroup;

    protected String bearerToken;
    protected String orgId;

    public AbstractMuleApi(String uri, Log log, String username, String password, String businessGroup)
    {
        super(log);
        this.uri = uri;
        this.username = username;
        this.password = password;
        this.businessGroup = businessGroup;
    }

    public void init() throws IOException {
        bearerToken = getBearerToken(username, password);
        orgId = getOrgId();
    }

    private String getBearerToken(String username, String password) throws IOException {
        // Supports either password or CS access token
        Pattern pattern = Pattern.compile(UUID_PATTERN_MATCHER);
        Matcher matcher = pattern.matcher(password);
        if (matcher.find()) {
            return password;
        }
        ObjectMapper mapper = new ObjectMapper();
        Entity<String> json = Entity.json("{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}");
        Response response = post(uri, LOGIN, json);
        validateStatusSuccess(response);
        AuthorizationResponse authorizationResponse = mapper.readValue(
                response.readEntity(String.class),
                AuthorizationResponse.class);
        return authorizationResponse.getAccessToken();
    }

    protected void validateStatusSuccess(Response response)
    {
        if (familyOf(response.getStatus()) != SUCCESSFUL)
        {
            throw new ApiException(response);
        }
    }

    public String getOrgId()
    {
        // Supports either Business Group ID or Business Group path
        Pattern pattern = Pattern.compile(UUID_PATTERN_MATCHER);
        Matcher matcher = pattern.matcher(businessGroup);
        if (matcher.find()) {
            return businessGroup;
        } else {
            return findBusinessGroup();
        }
    }

    @Override
    protected void configureRequest(Invocation.Builder builder)
    {
        if (bearerToken != null)
        {
            builder.header(AUTHORIZATION_HEADER, "bearer " + bearerToken);
        }

        if (orgId != null)
        {
            builder.header(ORG_ID_HEADER, orgId);
        }
    }

    private JSONObject getHierarchy()
    {
        UserInfo response = get(uri, ME, UserInfo.class);
        String rootOrgId = response.user.organization.getId();
        return new JSONObject(get(uri, "accounts/api/organizations/" + rootOrgId + "/hierarchy", String.class));
    }

    public String findBusinessGroup()
    {
        String currentOrgId = null;
        String[] groups = createBusinessGroupPath();
        JSONObject json = getHierarchy();
        JSONArray subOrganizations = (JSONArray) json.get("subOrganizations");
        if (groups.length == 0)
        {
            return (String) json.get("id");
        }
        if (json.get("name").equals(groups[0]))
        {
            currentOrgId = (String) json.get("id");
        }
        for (int group = 0; group < groups.length; group++)
        {
            for (int organization = 0; organization < subOrganizations.length(); organization++)
            {
                JSONObject jsonObject = (JSONObject) subOrganizations.get(organization);
                if (jsonObject.get("name").equals(groups[group]))
                {
                    currentOrgId = (String) jsonObject.get("id");
                    subOrganizations = (JSONArray) jsonObject.get("subOrganizations");
                }
            }
        }
        if (currentOrgId == null)
        {
            throw new ArrayIndexOutOfBoundsException("Cannot find business group.");
        }
        return currentOrgId;
    }

    protected String[] createBusinessGroupPath()
    {
        if (StringUtils.isEmpty(businessGroup))
        {
            return new String[0];
        }
        ArrayList<String> groups = new ArrayList<>();
        String group = "";
        int i = 0;
        for (; i < businessGroup.length() -1; i ++)
        {
            if (businessGroup.charAt(i) == '\\')
            {
                if (businessGroup.charAt(i+1) == '\\') // Double backslash maps to business group with one backslash
                {
                    group = group + "\\";
                    i++; // For two backslashes we continue with the next character
                }
                else // Single backslash starts a new business group
                {
                    groups.add(group);
                    group = "";
                }
            }
            else // Non backslash characters are mapped to the group
            {
                group = group + businessGroup.charAt(i);
            }
        }
        if (i < businessGroup.length()) // Do not end with backslash
        {
            group = group + businessGroup.charAt(businessGroup.length() - 1);
        }
        groups.add(group);
        return groups.toArray(new String[0]);
    }

}
