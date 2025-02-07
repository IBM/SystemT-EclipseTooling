create dictionary <ViewName>Dict
from file '<path to your dictionary here>'
with language as '<language_code(s)>';

create view <ViewName> as
extract dictionary <ViewName>Dict 
	on R.<input column> as match
from <input view> R;