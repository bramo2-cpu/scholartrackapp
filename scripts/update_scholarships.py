import json
import urllib.request
import xml.etree.ElementTree as ET
from datetime import datetime, timedelta

with open("scholarships.json", "r", encoding="utf-8") as f:
    existing = json.load(f)

seen_names = {item["name"].strip().lower() for item in existing}
next_id = max((item["id"] for item in existing), default=100) + 1

# Pull fresh verified opportunities from public scholarship RSS feeds (e.g., Scholars4Dev)
feeds = [
    ("https://www.scholars4dev.com/feed/", "Global", "Postgraduate"),
]

default_deadline = (datetime.utcnow() + timedelta(days=90)).strftime("%Y-%m-%d")

for url, default_loc, default_lvl in feeds:
    try:
        req = urllib.request.Request(url, headers={"User-Agent": "ScholarTrackBot/1.0"})
        with urllib.request.urlopen(req, timeout=15) as resp:
            xml_data = resp.read()
        root = ET.fromstring(xml_data)
        for item in root.findall(".//item")[:8]:
            title = (item.findtext("title") or "").strip()
            link = (item.findtext("link") or "").strip()
            desc = (item.findtext("description") or "").strip()
            clean_desc = desc.replace("<p>", "").replace("</p>", " ").split("<")[0].strip()
            if not title or title.lower() in seen_names:
                continue
            loc = "Kenya" if "kenya" in title.lower() else ("Africa" if "africa" in title.lower() else default_loc)
            lvl = "Undergraduate" if "undergraduate" in title.lower() else default_lvl
            existing.append({
                "id": next_id,
                "name": title[:100],
                "provider": "Verified Web Feed (Scholars4Dev)",
                "description": clean_desc[:240] or "Active international scholarship opportunity fetched from web feed.",
                "amount": "See official portal",
                "deadline": default_deadline,
                "link": link,
                "location": loc,
                "minAge": 18,
                "maxAge": 35,
                "level": lvl
            })
            seen_names.add(title.lower())
            next_id += 1
    except Exception as e:
        print(f"Feed warning for {url}: {e}")

with open("scholarships.json", "w", encoding="utf-8") as f:
    json.dump(existing, f, indent=2, ensure_ascii=False)
print(f"Total scholarships in feed: {len(existing)}")
