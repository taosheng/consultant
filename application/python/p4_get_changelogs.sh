p4user="Tao-Sheng_Chen"
P4PASSWD=$1
export P4PORT="10.201.16.19:1667"
p4client="`hostname`"
p4flag="-c $p4client -p $p4port -u $p4user"

# P4 login by autouser
~/bin/p4 $p4flag login

# Check out latest codes
#p4 sync

#~/bin/p4 $p4flag changes "@2013/01/15,@now" "//Ent/SSFB/Dev/SSFB_Server-2.1/..."
~/bin/p4 $p4flag  changes -L  "//Ent/SSFB/Dev/SSFB_Server-2.1/..."@2013/01/15,@now
