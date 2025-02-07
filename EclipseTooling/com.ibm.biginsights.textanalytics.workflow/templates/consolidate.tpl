create view <ViewName> as
select R.* 
from <input view> R
consolidate on R.<input column> using <consolidation policy>;