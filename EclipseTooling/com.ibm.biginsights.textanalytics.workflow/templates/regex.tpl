create view <ViewName> as
extract regex /<regular expression specification>/ 
	on R.<input column> as match
from <input view> R; 


