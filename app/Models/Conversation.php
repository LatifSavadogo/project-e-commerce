<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class Conversation extends Model
{
    protected $table = 'Conversation';
    protected $primaryKey = 'idconversation';
    public $timestamps = false;
    protected $guarded = [];
}
