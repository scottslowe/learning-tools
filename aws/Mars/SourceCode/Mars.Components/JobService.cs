using HtmlAgilityPack;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Http;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading.Tasks;

namespace Mars.Components
{
    public class JobService
    {
        public List<Job> GetJobs(string keywords, string where)
        {
            // default to toronto if where is empty or null
            if (string.IsNullOrEmpty(where))
            {
                where = "toronto";
            }

            string link = $"https://www.indeed.ca/jobs?q={keywords}&l={where}";
            List<Job> jobList = new List<Job>();

            // call http
            using (var client = new HttpClient())
            {
                var response = client.GetAsync(link).Result;
                string html = response.Content.ReadAsStringAsync().Result;

                var doc = new HtmlDocument();
                doc.LoadHtml(html);

                IEnumerable<HtmlNode> nodes = doc.DocumentNode.Descendants("div").Where(d => d.Attributes.Any(o => o.Name == "data-tn-component") && d.Attributes["data-tn-component"].Value.Contains("organicJob"));

                foreach (HtmlNode node in nodes)
                {
                    string title = GetTextFromNode(node, "h2/a");
                    string company = GetTextFromNode(node, "div/span[@class='company']");       // update the xPath
                    string location = GetTextFromNode(node, "span[@class='location']");
                    string summary = GetTextFromNode(node, "table/tr/td/div/span[@class='summary']");
                    string url = node.SelectSingleNode("h2/a").Attributes["href"].Value;

                    jobList.Add(new Job(title, company, location, summary, url));
                }
            }

            return jobList;
        }

        private string TrimAndRemoveNewLine(string text)
        {
            // change null to empty change to return
            if (text == null)
            {
                text = String.Empty;
            }

            return Regex.Replace(text.Trim(), @"\t|\n|\r", "");
        }

        private string GetTextFromNode(HtmlNode node, string xPath)
        {
            // add ? for null checking
            return TrimAndRemoveNewLine(node.SelectSingleNode(xPath)?.InnerText);
        }
    }
}
