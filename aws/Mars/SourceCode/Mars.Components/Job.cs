using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Mars.Components
{
    public class Job
    {
        [JsonProperty(PropertyName = "title")]
        public string Title { get; set; }

        [JsonProperty(PropertyName = "company")]
        public string Company { get; set; }

        [JsonProperty(PropertyName = "location")]
        public string Location { get; set; }

        [JsonProperty(PropertyName = "summary")]
        public string Summary { get; set; }

        [JsonProperty(PropertyName = "url")]
        public string Url { get; set; }

        public Job(string title, string company, string location, string summary, string url)
        {
            this.Title = title;
            this.Company = company;
            this.Location = location;
            this.Summary = summary;
            this.Url = $"https://www.indeed.ca/{url}";
        }
    }
}
