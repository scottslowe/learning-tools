using System.Linq;
using System.Net;
using System.Net.Http;
using System.Threading.Tasks;
using System.IO;
using Microsoft.Azure.WebJobs;
using Microsoft.Azure.WebJobs.Extensions.Http;
using Microsoft.Azure.WebJobs.Host;

using System.Net.Http.Headers;
using MarsWebSite.Components;

namespace MarsWebSite
{
    public static class Index
    {
        [FunctionName("Index")]
        public static async Task<HttpResponseMessage> Run(
            [HttpTrigger(AuthorizationLevel.Anonymous, "get", Route = null)]
            HttpRequestMessage req, TraceWriter log)
        {
            log.Info("Index request.");

            var response = new HttpResponseMessage(HttpStatusCode.OK);
            var stream = new FileStream(AppSettings.IndexPage, FileMode.Open);
            response.Content = new StreamContent(stream);
            response.Content.Headers.ContentType = new MediaTypeHeaderValue("text/html");
            return response;
        }

        [FunctionName("Page")]
        public static async Task<HttpResponseMessage> Page(
            [HttpTrigger(AuthorizationLevel.Anonymous, "get", 
            Route = "{page}")]
            HttpRequestMessage req, 
            string page, 
            TraceWriter log)
        {
            log.Info( page + " requested. " + req.RequestUri.AbsoluteUri);

            var response = new HttpResponseMessage(HttpStatusCode.OK);
            var stream = new FileStream(AppSettings.SiteRoot + @"\" + page, FileMode.Open);
            response.Content = new StreamContent(stream);
            response.Content.Headers.ContentType = new MediaTypeHeaderValue("text/html");
            return response;
        }

        [FunctionName("Site")]
        public static async Task<HttpResponseMessage> GetSiteContent([HttpTrigger(AuthorizationLevel.Anonymous, "get",
            Route = "Site/{path}/{file}")]
            HttpRequestMessage req, string path, string file, TraceWriter log)
        {
            var response = new HttpResponseMessage(HttpStatusCode.OK);
            var stream = new FileStream(AppSettings.SiteRoot + @"\" + path + @"\" + file, FileMode.Open);
            log.Info("file path is " + AppSettings.SiteRoot +@"\" + path + @"\" + file);
            response.Content = new StreamContent(stream);

            string contentType = "text/html";
            string fileType = System.IO.Path.GetExtension(file); //.Remove(0, 1).ToLower();
            contentType = MimeTypeMap.GetMimeType(fileType);         
            log.Info("content type is " + contentType);

            response.Content.Headers.ContentType = new MediaTypeHeaderValue(contentType);
            return response;
        }
    }
}
