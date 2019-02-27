using Mars.Components;
using Microsoft.Azure.WebJobs;
using Microsoft.Azure.WebJobs.Extensions.Http;
using Microsoft.Azure.WebJobs.Host;
using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Text;
using System.Threading.Tasks;

namespace MarsWebSite
{
    static public class Jobs
    {
        [FunctionName("Jobs")]
        public static HttpResponseMessage Run([HttpTrigger(AuthorizationLevel.Anonymous, "get",
            Route = "jobs")]HttpRequestMessage req, TraceWriter log)
        {
            log.Info("Jobs Request");

            // Parse query parameter
            string keywords = req.GetQueryNameValuePairs().FirstOrDefault(q => string.Compare(q.Key, "keywords", true) == 0).Value;
            string location = req.GetQueryNameValuePairs().FirstOrDefault(q => string.Compare(q.Key, "location", true) == 0).Value;

            JobService service = new JobService();
            List<Job> jobList = new List<Job>();

            if (!string.IsNullOrEmpty(keywords))
            {
                jobList.AddRange(service.GetJobs(keywords, location));
            }

            string json = JsonConvert.SerializeObject(jobList);

            return new HttpResponseMessage(HttpStatusCode.OK)
            {
                Content = new StringContent(json, Encoding.UTF8, "application/json")
            };
        }
    }
}
