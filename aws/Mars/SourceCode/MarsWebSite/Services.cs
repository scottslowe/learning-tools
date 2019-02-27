using System.Linq;
using System.Net;
using System.Net.Http;
using Microsoft.Azure.WebJobs;
using Microsoft.Azure.WebJobs.Extensions.Http;
using Microsoft.Azure.WebJobs.Host;
using MarsWebSite.Components;
using System.Net.Http.Headers;
using System;

namespace MarsWebSite
{
    public static class Services
    {
        private static SimpleCache _cache;
        static Services()
        {
            _cache = new SimpleCache();
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="req"></param>
        /// <param name="name">Container name</param>
        /// <param name="log"></param>
        /// <returns></returns>
        [FunctionName("Services")]
        public static HttpResponseMessage Run([HttpTrigger(AuthorizationLevel.Anonymous, "get",
            Route = "Services/{name}")]HttpRequestMessage req, string name, TraceWriter log)
        {
            // if user is authenticated and authorized, otherwise return nothing           
            string email = UserManager.GetAuthenticatedEmail();
            log.Info(email);

            string list = (string)_cache.Get(name);
            if (list == null)
            {
                StorageService service = new StorageService();
                list = service.ListFilesOrDirectories(name.Replace("-", "/"));

                int fileCacheInMinutes = AppSettings.FolderCacheInMinutes;
                _cache.Add(name,fileCacheInMinutes, list);
                log.Info("course folder refresh");
            }

            // Fetching the name from the path parameter in the request URL
            return req.CreateResponse(HttpStatusCode.OK, list, "application/json");
        }

        [FunctionName("GetFile")]
        public static HttpResponseMessage GetFile(
            [HttpTrigger(AuthorizationLevel.Anonymous, "get", 
            Route = "GetFile/{path}/{file}")]HttpRequestMessage req, 
            string path, string file, TraceWriter log)
        {
            path = path.Replace("-", "/");
            string email = UserManager.GetAuthenticatedEmail();
            StorageService service = new StorageService();
            log.Info(email);

            if (!UserManager.IsAutenticated())
            {
                // redirect to login
                log.Info("redirect to login .");
                var response = req.CreateResponse();

                response.Headers.Add("location", "/.auth/login/microsoftaccount?post_login_redirect_uri=" + req.RequestUri.AbsoluteUri);
                response.StatusCode = HttpStatusCode.Redirect;

                return response;
            }
            else {
                // authenticated user, check if it's allowed to access this folder
                if (service.IsAllowDownload(email, path))
                {
                    log.Info("IsAllowDownload == true " + email + " " + path);
                    var content = service.GetContent(path, file);
                    System.IO.MemoryStream stream = new System.IO.MemoryStream();
                    content.DownloadToStream(stream, null);
                    stream.Seek(0, System.IO.SeekOrigin.Begin);
                    var response = new HttpResponseMessage(HttpStatusCode.OK);

                    response.Content = new StreamContent(stream);
                    response.Content.Headers.ContentDisposition = new ContentDispositionHeaderValue("inline");

                    string fileType = System.IO.Path.GetExtension(file);
                    string contentType = MimeTypeMap.GetMimeType(fileType);
                    response.Content.Headers.ContentType = new MediaTypeHeaderValue(contentType);

                    return response;
                }
                else
                {
                    log.Info("IsAllowDownload == false " + email + " " + path);
                    return req.CreateResponse(HttpStatusCode.OK, "Access Denied");
                }
            }
        }

        [FunctionName("Enrollment")]
        public static HttpResponseMessage GetEnrollment(
            [HttpTrigger(AuthorizationLevel.Anonymous, "get",
            Route = "Enrollment")]HttpRequestMessage req, TraceWriter log) {

            string email = UserManager.GetAuthenticatedEmail();
            if (email == null) {
                log.Warning("Services.GetEnrollment unauthenciated user.");
            }
            log.Info(email);

            StorageService service = new StorageService();
            string list = service.GetEnrollment(email);
            
            return req.CreateResponse(HttpStatusCode.OK, list, "application/json");
        }

        [FunctionName("GetUser")]
        public static HttpResponseMessage GetUser(
           [HttpTrigger(AuthorizationLevel.Anonymous, "get",
            Route = "User")]HttpRequestMessage req, TraceWriter log)
        {
            string user = UserManager.GetAuthenticatedUser();
            if (user == null)
            {
                log.Warning("Services.GetEnrollment unauthenciated user.");
            }
            log.Info(user);                         

            return req.CreateResponse(HttpStatusCode.OK, user);
        }

        [FunctionName("EndSession")]
        public static HttpResponseMessage EndSession(
            [HttpTrigger(AuthorizationLevel.Anonymous, 
            "get", Route = "EndSession")]HttpRequestMessage req, 
            TraceWriter log)
        {
            var response = req.CreateResponse();
            response.Headers.Add("location", "/index");
            response.StatusCode = HttpStatusCode.Redirect;
    
            try
            {
                if (req.Headers != null)
                {
                    //remove claims header
                    foreach (var header in req.Headers)
                    {
                        log.Info("Header " + header.Key);
                        if (header.Key.Contains("X-MS-TOKEN-"))
                        {
                            response.Headers.Remove(header.Key);
                            log.Info("removed the header " + header.Key);
                        }
                    }

                    //remove cookies
                    var reqCookies = req.Headers.GetCookies();
                    CookieHeaderValue[] respCookies;
                    if (reqCookies != null) {
                        respCookies = new CookieHeaderValue[reqCookies.Count];
                        for (int i = 0; i < reqCookies.Count; i++) {
                            respCookies[i] = new CookieHeaderValue(reqCookies[i].Cookies[0].Name, reqCookies[i].Cookies[0].Value);
                            respCookies[i].Expires =  DateTime.Now.AddDays(-5);
                            log.Info("expire cookie " + reqCookies[i].Cookies[0].Name);
                        }
                        response.Headers.AddCookies(respCookies);
                    }
                }
            }
            catch {
                log.Error("removing claims header/cookie error");
            }
            return response;
        }
    }
}
