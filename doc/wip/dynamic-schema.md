# Power of the flexible schema

MapR-DB supports OJAI documents as a native data store. An OJAI document is a tree of fields. Each field has a type and 
a value, and also has either a name or an array index. Field names are strings. The root of each document is a map. The 
structure of each document, called the document's schema, is easy to change. We can simply add new fields. Lets consider 
the example below.

### MapR Music - Document Update Information
Suppose, the application is almost done and developer team receives the next new requirement: "Can you now, add the 
information about the user that creates the document, and or the last modification of the document". As you will see, it 
really easy to implement this new feature, without affecting too much code.

### Requirement
Album/Artist documents should contain update information after document creation and modification. Below, you can see an 
example of such document update information:

```
{

 ...
 
 "update_information" : {
     "date_of_operation" : 1507722451221,
     "user_id" : "music"
 }

 ...
}
```

It contains two fields:
* `date_of_operation` - timestamp of document creation/modification.
* `user_id` - identifier of document's creator/editor.

### Implementation

1. Accessing user's `Principal`

In order to get identifier of authenticated user we have to get user's `java.security.Principal`. In JAX-RS has 
`javax.ws.rs.core.SecurityContext` interface, which declares method for determining the identity of the user making the
secured HTTP invocation. It also has a method that allows you to check whether or not the current user has a certain 
role:
```
public interface SecurityContext {

   public Principal getUserPrincipal();
   public boolean isUserInRole(String role);
   public boolean isSecure();
   public String getAuthenticationScheme();
}
```

You get access to a `SecurityContext` instance by injecting it into a field, setter method, or resource method 
parameter using the `@Context` annotation. Since `SecurityContext` can be injected only into JAX-RS components, we can 
access it only at presentation layer(contains classes, annotated with `javax.ws.rs.Path` annotations).

In order to make user's `Principal` accessible at each layer of the app, we will use 
`javax.ws.rs.container.ContainerRequestFilter` and `ResteasyProviderFactory`. Below you can see the listing of 
`UserPrincipalContextFilter`, which has access to the `SecurityContext`. It uses the RESTeasy utility class to store the 
`Principal` at the `ResteasyProviderFactory` context:
```
package com.mapr.music.util;

import org.jboss.resteasy.spi.ResteasyProviderFactory;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.security.Principal;

/**
 * Class which has access to the SecurityContext. Uses the RESTeasy utility class to push the User Principal into the
 * context. This allows to access User Principal at other components.
 */
@Provider
public class UserPrincipalContextFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext context) throws IOException {
        SecurityContext securityContext = context.getSecurityContext();
        ResteasyProviderFactory.pushContext(Principal.class, securityContext.getUserPrincipal());
    }
}
```

After that, we can access user's `Principal` in the following way:
```
Principal principal = ResteasyProviderFactory.getContextData(Principal.class);
```

2. Constructing Document Update Information

Lets add the new method to the `MaprDbDaoImpl.java`, which will construct Document Update Information if such info is 
available:
```
    /**
     * Constructs and returns map, which contains document update information.
     *
     * @return map, which contains document update information.
     */
    protected Optional<Map<String, Object>> getUpdateInfo() {

        Principal principal = ResteasyProviderFactory.getContextData(Principal.class);
        if (principal == null) {
            return Optional.empty();
        }

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("user_id", principal.getName());
        userInfo.put("date_of_operation", System.currentTimeMillis());

        return Optional.of(userInfo);
    }
```

3. Storing Document Update Information

After that we can use Document Update Information along with OJAI `org.ojai.Document` and 
`org.ojai.store.DocumentMutation` in the following way:
```
  ...
  // Set update info if available
  getUpdateInfo().ifPresent(updateInfo -> albumMutation.set("update_info", updateInfo));
  ...
```

So, we can implement required feature by changing `create` and `update` methods of `AlbumDaoImpl` and `ArtistDaoImpl` 
classes.
