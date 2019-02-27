using System.Linq;
using System.Net;
using System.Net.Http;
using Microsoft.Azure.WebJobs;
using Microsoft.Azure.WebJobs.Extensions.Http;
using Microsoft.Azure.WebJobs.Host;
using MarsWebSite.Components;

namespace MarsWebSite
{
    public static class Courses
    {
        private static SimpleCache _cache;
        static Courses() {
            _cache = new SimpleCache();
        }
        [FunctionName("Courses")]
        public static HttpResponseMessage Run([HttpTrigger(AuthorizationLevel.Anonymous, "get", 
            Route = "courses")]HttpRequestMessage req, TraceWriter log)
        {
            log.Info("Courses Request");

            string course = (string)_cache.Get("GetCourses");
            if ( course == null)
            {
                StorageService service = new StorageService();
                course = service.GetCourses();
                int cacheInMinutes = AppSettings.CourseCacheInMinutes;
                _cache.Add("GetCourses", cacheInMinutes, course);
                log.Info("course refresh");
            }

            // Fetching the name from the path parameter in the request URL
            return req.CreateResponse(HttpStatusCode.OK, course, "application/json");
        }
    }
}
