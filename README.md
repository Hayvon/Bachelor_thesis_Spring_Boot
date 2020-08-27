# Bachelor thesis: Spring Boot 

<p>This application implements the workflow automation of the given BPMN diagram using <b>Camunda BPM</b> and  <b>Spring Boot</b>. This application serves as a basis for the comparison of my bachelor thesis. It shall be compared, which advantages and disadvantages the realization of the same project in <b>Micronaut</b> and <b>Quarkus</b> brings. </p>
<img src="https://i.imgur.com/bW1R1rg.png">
<p>This application has no frontend, so it is controlled by the REST API. For POST requests <a href="https://www.postman.com/">Postman</a> can be used. By sending a holiday request the workflow will be started. The following task  <b>"Urlaubsantrag überprüfen"</b> is a humantask, which is first assigned to a user and then processed by the user. (Rejection or confirmation of the holiday  request). Afterwards an automatic message about the outcome of the holiday request is generated by a service task. This workflow automation is by no means complete. It is only intended for comparison with Micronaut and Quarkus. </p>
<h2>Rest-API:</h2>
<ul>
    <li><b>POST /api/Holidayrequests/create</b></li>
    <p>Creates holidayrequest and starts workflow. "fullName", "vorgesetzter", "startDate", "endDate" and "status" must be passed in the body using JSON format.</p>
    <li><b>GET /api/Holidayrequests</b></li>
    <p>Shows all holidayrequests</p>
    <li><b>GET /api/Users</b></li>
    <p>Shows all users.</p>
    <li><b>GET /api/Users/{id}</b></li>
    <p>Shows specific user.</p>
    <li><b>POST/api/Users/create</b></li>
    <p>Creates an user. "name" must be passed in the body using JSON format.</p>
    <li><b>GET /api/Holidayrequests/{id}</b></li>
    <p>Shows specific holidayrequest.</p>
    <li><b>POST /api/Holidayrequests/{id}/assign</b></li>
    <p>Assigns a user to a specific holidayrequest. "userid" and "name" must be passed in the body using JSON format.</p>
    <li><b>POST /api/Holidayrequests/{id}/approve</b></li>
    <p>Confirms a specific holidayrequest."userid" and"name" must be passed in the body using JSON format. </p>
    <li><b>POST /api/Holidayrequests/{id}/reject</b></li>
    <p>Rejects a specific holidayrequest."userid" and "name" must be passed in the body using JSON format.</p>
</ul>

<h2>Start application in Docker:</h2>
<ul>
 <li>"run.sh" creates docker container from the docker file.</li>
  <li>"remove.sh" kills and deletes docker container.</li>
</ul>
</body>
</html>
