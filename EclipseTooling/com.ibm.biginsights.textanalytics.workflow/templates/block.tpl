create view <ViewName> as
extract blocks 
	with count <count specification>
	and separation <separation specification> 
	on R.<input column> as match
from <input view> R;  