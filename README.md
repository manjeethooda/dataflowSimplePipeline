Add the following before running the project.

File : application.properties:
                gcp.projectID

File : Pom.xml
                <gcp.projectID></gcp.projectID>
                <gcs.urlBase></gcs.urlBase>
                <gcs.bucketName></gcs.bucketName>

File : Simplepipeline
                RedisIO.write().withEndpoint("",)