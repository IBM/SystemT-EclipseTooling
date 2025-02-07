create view <ViewName> as
(select R.* from <input view containing all candidates> R)
minus
(select R.* from <input view containing invalid candidates> R);  